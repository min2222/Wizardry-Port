package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityFlying;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.EntityAIHurtByTarget;
import net.minecraft.world.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.world.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntityZombieMinion extends EntityZombie implements ISummonedCreature {

	private static final EntityDataSerializer<Boolean> SPAWN_PARTICLES = SynchedEntityData.createKey(EntityZombieMinion.class, EntityDataSerializers.BOOLEAN);

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new zombie minion in the given world. */
	public EntityZombieMinion(Level world){
		super(world);
		this.experienceValue = 0;
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(SPAWN_PARTICLES, true);
	}

	// EntityZombie overrides (EntityZombie is a complex class so there are lots of these)

	@Override
	protected void applyEntityAI(){
		this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	@Override public boolean isChild(){ return false; }
	@Override public void setChild(boolean childZombie){} // Can't be a child
	@Override protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty){} // They don't have equipment!
	@Override public void onKillEntity(LivingEntity entityLivingIn){} // Turns villagers to zombies in EntityZombie
	@Override public void setChildSize(boolean isChild){}
	@Override protected ItemStack getSkullDrop(){ return ItemStack.EMPTY; }

	// Implementations

	@Override
	public void setRevengeTarget(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		this.updateDelegate();
	}

	@Override
	public void onSpawn(){
		if(this.dataManager.get(SPAWN_PARTICLES)) this.spawnParticleEffect();
		if(shouldBurnInDay() && getCaster() instanceof Player
				&& ItemArtefact.isArtefactActive((Player)getCaster(), WizardryItems.charm_undead_helmets)){
			setItemStackToSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				this.world.spawnParticle(ParticleTypes.SMOKE_LARGE, this.posX + this.rand.nextFloat() - 0.5f,
						this.posY + this.rand.nextFloat() * 2, this.posZ + this.rand.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	public boolean hasAnimation(){
		return this.dataManager.get(SPAWN_PARTICLES) || this.ticksExisted > 20;
	}

	public void hideParticles(){
		this.dataManager.set(SPAWN_PARTICLES, false);
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

	@Override
	public boolean getCanSpawnHere(){
		return this.world.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	public boolean canAttackClass(Class<? extends LivingEntity> entityType){
		// Returns true unless the given entity type is a flying entity.
		return !EntityFlying.class.isAssignableFrom(entityType);
	}

	@Override
	public Component getDisplayName(){
		if(getCaster() != null){
			return new TextComponentTranslation(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					new TextComponentTranslation("entity." + this.getEntityString() + ".name"));
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