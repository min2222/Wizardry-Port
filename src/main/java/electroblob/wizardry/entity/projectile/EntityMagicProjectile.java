package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.RayTracer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

/**
 * This class is a generic superclass for all <b>non-directed</b> projectiles, namely: darkness orb, firebolt, firebomb,
 * force orb, ice charge, lightning disc, poison bomb, spark, spark bomb and thunderbolt. Directed (arrow-like)
 * projectiles should instead extend {@link EntityMagicArrow}.
 * <p></p>
 * This class purely handles saving of the damage multiplier; EntityThrowable is pretty well suited to my purposes as it
 * is. Range is done via the velocity when the constructor is called. Caster is already handled by
 * EntityThrowable.getThrower(), though due to a bug in vanilla it has to be synced by this class.
 * 
 * @since Wizardry 1.0
 * @author Electroblob
 * @see EntityBomb
 */
public abstract class EntityMagicProjectile extends ThrowableProjectile implements IEntityAdditionalSpawnData {

	public static final double LAUNCH_Y_OFFSET = 0.1;
	public static final int SEEKING_TIME = 15;

	public float damageMultiplier = 1.0f;

	/** Creates a new projectile in the given world. */
	public EntityMagicProjectile(EntityType<? extends ThrowableProjectile> type, Level world){
		super(type, world);
	}

	// Initialiser methods
	
	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it in the direction they are looking with the given speed. */
	public void aim(LivingEntity caster, float speed){
		this.setPos(caster.getX(), caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET, caster.getZ());
		// This is the standard set of parameters for this method, used by snowballs and ender pearls amongst others.
		this.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0f, speed, 1.0f);
		this.setOwner(caster);
	}

	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it at the given target with the given speed. The trajectory will be altered slightly by a random amount
	 * determined by the aimingError parameter. For reference, skeletons set this to 10 on easy, 6 on normal and 2 on hard
	 * difficulty. */
	public void aim(LivingEntity caster, Entity target, float speed, float aimingError){
		
		this.setOwner(caster);

		this.setPos(this.getX(), caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET, this.getZ());
		double dx = target.getX() - caster.getX();
		double dy = !this.isNoGravity() ? target.getY() + (double)(target.getBbHeight() / 3.0f) - this.getY()
				: target.getY() + (double)(target.getBbHeight() / 2.0f) - this.getY();
		double dz = target.getZ() - caster.getZ();
		double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

		if(horizontalDistance >= 1.0E-7D){
			
			double dxNormalised = dx / horizontalDistance;
			double dzNormalised = dz / horizontalDistance;
			this.setPos(caster.getX() + dxNormalised, this.getY(), caster.getZ() + dzNormalised);

			// Depends on the horizontal distance between the two entities and accounts for bullet drop,
			// but of course if gravity is ignored this should be 0 since there is no bullet drop.
			float bulletDropCompensation = !this.isNoGravity() ? (float)horizontalDistance * 0.2f : 0;
			// It turns out that this method normalises the input (x, y, z) anyway
			this.shoot(dx, dy + (double)bulletDropCompensation, dz, speed, aimingError);
		}
	}

	public void setCaster(LivingEntity caster){
		this.setOwner(caster);
	}

	/**
	 * Returns the seeking strength of this projectile, or the maximum distance from a target the projectile can be
	 * heading for that will make it curve towards that target. By default, this is 2 if the caster is wearing a ring
	 * of attraction, otherwise it is 0.
	 */
	public float getSeekingStrength(){
		return getOwner() instanceof Player && ItemArtefact.isArtefactActive((Player)getOwner(),
				WizardryItems.ring_seeking) ? 2 : 0;
	}

	@Override
	public void tick(){

		super.tick();

		if(getLifetime() >=0 && this.tickCount > getLifetime()){
			this.discard();
		}

		// Seeking
		if(getSeekingStrength() > 0){

			Vec3 velocity = this.getDeltaMovement();

			HitResult hit = RayTracer.rayTrace(level, this.position(),
					this.position().add(velocity.scale(SEEKING_TIME)), getSeekingStrength(), false,
					true, false, LivingEntity.class, RayTracer.ignoreEntityFilter(null));

			Entity entityHit = hit.getType() == Type.ENTITY ? ((EntityHitResult) hit).getEntity() : null;
			if(hit != null && entityHit != null){

				if(AllyDesignationSystem.isValidTarget(getOwner(), entityHit)){

					Vec3 direction = new Vec3(entityHit.getX(), entityHit.getY() + entityHit.getBbHeight()/2,
							entityHit.getZ()).subtract(this.position()).normalize().scale(velocity.length());
					this.setDeltaMovement(this.getDeltaMovement().add(2 * (direction.x - this.getDeltaMovement().x) / SEEKING_TIME, 2 * (direction.y - this.getDeltaMovement().y) / SEEKING_TIME, 2 * (direction.z - this.getDeltaMovement().z) / SEEKING_TIME));
				}
			}
		}
	}
	
	@Override
	protected void defineSynchedData() {
		
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf data){
		data.writeInt(this.getOwner() == null ? -1 : this.getOwner().getId());
	}

	@Override
	public void readSpawnData(FriendlyByteBuf data){
		int id = data.readInt();
		if(id == -1) return;
		Entity entity = this.level.getEntity(id);
		this.setOwner(entity);
	}
	
	@Override
	public SoundSource getSoundSource(){
		return WizardrySounds.SPELLS;
	}

	/** Returns the maximum flight time in ticks before this projectile disappears, or -1 if it can continue
	 * indefinitely until it hits something. This should be constant. */
	public abstract int getLifetime();

}
