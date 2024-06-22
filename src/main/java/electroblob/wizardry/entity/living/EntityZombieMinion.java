package electroblob.wizardry.entity.living;

import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityZombieMinion extends Zombie implements ISummonedCreature {

	private static final EntityDataAccessor<Boolean> SPAWN_PARTICLES = SynchedEntityData.defineId(EntityZombieMinion.class, EntityDataSerializers.BOOLEAN);

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerUUID(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new zombie minion in the given world. */
	public EntityZombieMinion(Level world){
		this(WizardryEntities.ZOMBIE_MINION.get(), world);
		this.xpReward = 0;
	}
	
	public EntityZombieMinion(EntityType<? extends Zombie> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		this.entityData.define(SPAWN_PARTICLES, true);
	}

	// EntityZombie overrides (EntityZombie is a complex class so there are lots of these)

	@Override
	protected void registerGoals(){
		this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, false, 4, this::canBreakDoors));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	@Override public boolean isBaby(){ return false; }
	@Override public void setBaby(boolean childZombie){} // Can't be a child
	@Override protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance difficulty){} // They don't have equipment!
	@Override public boolean wasKilled(ServerLevel level, LivingEntity entityLivingIn){ return false; } // Turns villagers to zombies in EntityZombie
	@Override protected ItemStack getSkull(){ return ItemStack.EMPTY; }

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
		if(this.entityData.get(SPAWN_PARTICLES)) this.spawnParticleEffect();
		if(isSunSensitive() && getCaster() instanceof Player
				&& ItemArtefact.isArtefactActive((Player)getCaster(), WizardryItems.CHARM_UNDEAD_HELMETS.get())){
			setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.level.isClientSide){
			for(int i = 0; i < 15; i++){
				this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + this.random.nextFloat() - 0.5f,
						this.getY() + this.random.nextFloat() * 2, this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	public boolean hasAnimation(){
		return this.entityData.get(SPAWN_PARTICLES) || this.tickCount > 20;
	}

	public void hideParticles(){
		this.entityData.set(SPAWN_PARTICLES, false);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
        return this.interactDelegate(player, hand) == InteractionResult.FAIL ? super.mobInteract(player, hand) : this.interactDelegate(player, hand);
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

	@Override public int getExperienceReward(){ return 0; }
	@Override protected boolean shouldDropLoot(){ return false; }
	@Override protected ResourceLocation getDefaultLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override public boolean removeWhenFarAway(double distance){
		return getCaster() == null && getOwnerUUID() == null;
	}

	@Override
	public boolean canAttack(LivingEntity entityType){
		// Returns true unless the given entity type is a flying entity.
		return !(entityType instanceof FlyingMob);
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() != null){
			return Component.translatable(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					Component.translatable("entity." + this.getEncodeId() + ".name"));
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