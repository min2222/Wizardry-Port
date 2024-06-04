package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.RayTracer;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EntityThrowable;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

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
public abstract class EntityMagicProjectile extends EntityThrowable implements IEntityAdditionalSpawnData {

	public static final double LAUNCH_Y_OFFSET = 0.1;
	public static final int SEEKING_TIME = 15;

	public float damageMultiplier = 1.0f;

	/** Creates a new projectile in the given world. */
	public EntityMagicProjectile(Level world){
		super(world);
	}

	// Initialiser methods
	
	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it in the direction they are looking with the given speed. */
	public void aim(LivingEntity caster, float speed){
		this.setPosition(caster.getX(), caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET, caster.getZ());
		// This is the standard set of parameters for this method, used by snowballs and ender pearls amongst others.
		this.shoot(caster, caster.rotationPitch, caster.rotationYaw, 0.0f, speed, 1.0f);
		this.thrower = caster;
		// Mojang's 'fix' for the projectile-hitting-thrower bug actually made the problem worse, hence the following line.
		this.ignoreEntity = caster;
	}

	/** Sets the shooter of the projectile to the given caster, positions the projectile at the given caster's eyes and
	 * aims it at the given target with the given speed. The trajectory will be altered slightly by a random amount
	 * determined by the aimingError parameter. For reference, skeletons set this to 10 on easy, 6 on normal and 2 on hard
	 * difficulty. */
	public void aim(LivingEntity caster, Entity target, float speed, float aimingError){
		
		this.thrower = caster;
		// Mojang's 'fix' for the projectile-hitting-thrower bug actually made the problem worse, hence the following line.
		this.ignoreEntity = thrower;

		this.getY() = caster.getY() + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET;
		double dx = target.getX() - caster.getX();
		double dy = !this.hasNoGravity() ? target.getY() + (double)(target.getBbHeight() / 3.0f) - this.getY()
				: target.getY() + (double)(target.getBbHeight() / 2.0f) - this.getY();
		double dz = target.getZ() - caster.getZ();
		double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

		if(horizontalDistance >= 1.0E-7D){
			
			double dxNormalised = dx / horizontalDistance;
			double dzNormalised = dz / horizontalDistance;
			this.setPosition(caster.getX() + dxNormalised, this.getY(), caster.getZ() + dzNormalised);

			// Depends on the horizontal distance between the two entities and accounts for bullet drop,
			// but of course if gravity is ignored this should be 0 since there is no bullet drop.
			float bulletDropCompensation = !this.hasNoGravity() ? (float)horizontalDistance * 0.2f : 0;
			// It turns out that this method normalises the input (x, y, z) anyway
			this.shoot(dx, dy + (double)bulletDropCompensation, dz, speed, aimingError);
		}
	}

	public void setCaster(LivingEntity caster){
		this.thrower = caster;
		this.ignoreEntity = caster;
	}

	/**
	 * Returns the seeking strength of this projectile, or the maximum distance from a target the projectile can be
	 * heading for that will make it curve towards that target. By default, this is 2 if the caster is wearing a ring
	 * of attraction, otherwise it is 0.
	 */
	public float getSeekingStrength(){
		return getThrower() instanceof Player && ItemArtefact.isArtefactActive((Player)getThrower(),
				WizardryItems.ring_seeking) ? 2 : 0;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(getLifetime() >=0 && this.ticksExisted > getLifetime()){
			this.discard();
		}

		// Seeking
		if(getSeekingStrength() > 0){

			Vec3 velocity = new Vec3(motionX, motionY, motionZ);

			HitResult hit = RayTracer.rayTrace(world, this.getPositionVector(),
					this.getPositionVector().add(velocity.scale(SEEKING_TIME)), getSeekingStrength(), false,
					true, false, LivingEntity.class, RayTracer.ignoreEntityFilter(null));

			if(hit != null && hit.entityHit != null){

				if(AllyDesignationSystem.isValidTarget(getThrower(), hit.entityHit)){

					Vec3 direction = new Vec3(hit.entityHit.getX(), hit.entityHit.getY() + hit.entityHit.getBbHeight()/2,
							hit.entityHit.getZ()).subtract(this.getPositionVector()).normalize().scale(velocity.length());

					motionX = motionX + 2 * (direction.x - motionX) / SEEKING_TIME;
					motionY = motionY + 2 * (direction.y - motionY) / SEEKING_TIME;
					motionZ = motionZ + 2 * (direction.z - motionZ) / SEEKING_TIME;
				}
			}
		}
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(this.getThrower() == null ? -1 : this.getThrower().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){
		int id = data.readInt();
		if(id == -1) return;
		Entity entity = this.world.getEntityByID(id);
		if(entity instanceof LivingEntity) this.thrower = (LivingEntity)entity;
		this.ignoreEntity = this.thrower;
	}
	
	@Override
	public SoundSource getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

	/** Returns the maximum flight time in ticks before this projectile disappears, or -1 if it can continue
	 * indefinitely until it hits something. This should be constant. */
	public abstract int getLifetime();

}
