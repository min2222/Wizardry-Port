package electroblob.wizardry.entity.living;

import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IEntityLivingData;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

/** As of Wizardry 1.2, this is now an ISummonedCreature like the rest of them, and it extends EntitySlime. */
public class EntityMagicSlime extends EntitySlime implements ISummonedCreature {

	// Field implementations
	private int lifetime = 200;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	public EntityMagicSlime(Level world){
		super(world);
		// TESTME: Should this be true or false? Has something to do with health.
		this.setSlimeSize(2, false); // Needs to be called before setting the experience value to 0
		this.experienceValue = 0;
	}

	/**
	 * Creates a new magic slime with the given caster and lifetime, riding the given target.
	 * 
	 * @param world The world that the slime is in.
	 * @param caster The entity that created the slime.
	 * @param target The slime's victim. The slime will automatically start riding this entity.
	 * @param lifetime The number of ticks before the slime bursts.
	 */
	public EntityMagicSlime(Level world, LivingEntity caster, LivingEntity target, int lifetime){
		super(world);
		this.setPosition(target.getX(), target.getY(), target.getZ());
		this.startRiding(target);
		if (caster != null) this.setOwnerId(caster.getUniqueID());
		this.setSlimeSize(2, false); // Needs to be called before setting the experience value to 0
		this.experienceValue = 0;
		this.lifetime = lifetime;
	}

	// EntitySlime overrides

	@Override protected void initEntityAI(){} // Has no AI!
	@Override protected void dealDamage(LivingEntity entity){} // Handles damage itself

	@Override
	public void discard(){
		// Restores behaviour from Entity, replacing slime splitting behaviour.
		this.isDead = true;
		// Makes sure that the undoing in onUpdate won't undo this. For some reason, EntitySlime sets isDead directly
		// to do the peaceful despawning, which seems odd but is actually rather handy!
		this.setHealth(0);
		// Bursting effect
		for(int i = 0; i < 30; i++){
			double x = this.getX() - 0.5 + random.nextDouble();
			double y = this.getY() - 0.5 + random.nextDouble();
			double z = this.getZ() - 0.5 + random.nextDouble();
			this.world.spawnParticle(ParticleTypes.SLIME, x, y, z, (x - this.getX()) * 2, (y - this.getY()) * 2,
					(z - this.getZ()) * 2);
		}
		this.playSound(WizardrySounds.ENTITY_MAGIC_SLIME_SPLAT, 2.5f, 0.6f);
		this.playSound(WizardrySounds.ENTITY_MAGIC_SLIME_EXPLODE, 1.0f, 0.5f);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata){
		// Removes size randomisation
		IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
		this.setSlimeSize(2, false);
		return data;
	}

	@Override
	public boolean hurt(DamageSource source, float amount){
		// Immune to suffocation
		return source == DamageSource.IN_WALL ? false : super.hurt(source, amount);
	}

	// Implementations

	@Override
	public void setRevengeTarget(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
	}

	@Override
	public void onUpdate(){

		super.onUpdate();
		// Undoes the despawning on peaceful behaviour. I don't think there's anything in super.onUpdate that sets
		// isDead other than that, but it's better to do a quick sanity check just to be sure.
		if(this.isDead && world.getDifficulty() == Difficulty.PEACEFUL && this.getHealth() > 0) this.isDead = false;
		// Bursts instantly rather than doing the falling over animation.
		if(this.getHealth() <= 0) this.discard();

		this.updateDelegate();

		// Damages and slows the slime's victim or makes the slime explode if the victim is dead.
		if(this.getRidingEntity() != null && this.getRidingEntity() instanceof LivingEntity
				&& ((LivingEntity)this.getRidingEntity()).getHealth() > 0){
			if(this.ticksExisted % 16 == 1){
				this.getRidingEntity().hurt(DamageSource.MAGIC, 1);
				if(this.getRidingEntity() != null){ // Some mobs force-dismount when attacked (normally when dying)
					((LivingEntity)this.getRidingEntity())
							.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 2));
				}
				this.playSound(WizardrySounds.ENTITY_MAGIC_SLIME_ATTACK, 1.0f, 1.0f);
				this.squishAmount = 0.5F;
			}
		}else{
			this.discard();
		}
	}

	@Override
	public void onSpawn(){
	}

	@Override
	public void onDespawn(){
	}

	@Override
	public boolean hasParticleEffect(){
		return false;
	}

	@Override
	public boolean hasAnimation(){
		return false;
	}

	@Override
	protected boolean processInteract(Player player, InteractionHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override protected int getExperiencePoints(Player player){ return 0; }
	@Override protected boolean canDropLoot(){ return false; }
	@Override protected Item getDropItem(){ return null; }
	@Override protected ResourceLocation getLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override protected boolean canDespawn(){
		return getCaster() == null && getOwnerId() == null;
	}

}
