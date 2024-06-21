package electroblob.wizardry.entity.living;

import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.WizardryEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EntityBlazeMinion extends Blaze implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override
	public int getLifetime(){
		return lifetime;
	}

	@Override
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public UUID getOwnerUUID(){
		return casterUUID;
	}

	@Override
	public void setOwnerId(UUID uuid){
		this.casterUUID = uuid;
	}

	/** Creates a new blaze minion in the given world. */
	public EntityBlazeMinion(Level world){
		this(WizardryEntities.BLAZE_MINION.get(), world);
		this.xpReward = 0;
	}
	
	public EntityBlazeMinion(EntityType<? extends Blaze> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	// EntityBlaze overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void registerGoals(){
		super.registerGoals();
		this.goalSelector.removeAllGoals();
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	// Implementations

	@Override
	public void setLastHurtByMob(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setLastHurtByMob(entity);
	}

	@Override
	public void tick(){
		super.tick();
		this.updateDelegate();
	}

	@Override
	public void onSpawn(){
		this.spawnParticleEffect();
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	/**
	 * Normally this would be private, but since this class has subclasses with different spawn/despawn particle
	 * effects, it makes sense to have them override this rather than both onSpawn() and onDespawn().
	 */
	protected void spawnParticleEffect(){
		if(this.level.isClientSide){
			for(int i = 0; i < 15; i++){
				this.level.addParticle(ParticleTypes.FLAME, this.getX() + this.random.nextFloat() - 0.5f,
						this.getY() + this.random.nextFloat() * getBbHeight(), this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return false;
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffdd4d, 0xff6600, animationProgress);
	}

	@Override
	protected boolean processInteract(Player player, InteractionHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's 'stop as soon as you find true' method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
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
		return getCaster() == null && getOwnerUUID() == null;
	}

	@Override
	public boolean getCanSpawnHere(){
		return this.level.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	public boolean canAttack(LivingEntity entityType){
		return true;
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() != null){
			return Component.translatable(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					Component.translatable("entity." + this.getEntityString() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getCaster() != null;
	}
}
