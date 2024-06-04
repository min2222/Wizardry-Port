package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.world.entity.EntityFlying;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IEntityLivingData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.EntityAIHurtByTarget;
import net.minecraft.world.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EntityWitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.UUID;

public class EntityWitherSkeletonMinion extends EntityWitherSkeleton implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new wither skeleton minion in the given world. */
	public EntityWitherSkeletonMinion(Level world){
		super(world);
		this.experienceValue = 0;
	}

	// EntitySkeleton overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<LivingEntity>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	// Shouldn't have randomised armour, but does still need a sword!
	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty){
		this.setItemStackToSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata){
		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getEntityAttribute(Attributes.FOLLOW_RANGE)
				.applyModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, EntityUtils.Operations.MULTIPLY_FLAT));

		if(this.random.nextFloat() < 0.05F){
			this.setLeftHanded(true);
		}else{
			this.setLeftHanded(false);
		}
		
		this.setItemStackToSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);

		// Halloween pumpkin heads! Why not?
		if(this.getItemStackFromSlot(EquipmentSlot.HEAD).isEmpty()){
			Calendar calendar = this.world.getCurrentDate();

			if(calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F){
				this.setItemStackToSlot(EquipmentSlot.HEAD,
						new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
				this.inventoryArmorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
			}
		}

		return livingdata;
	}

	// Implementations

	@Override
	public void setRevengeTarget(LivingEntity entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
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

	private void spawnParticleEffect(){
		if(this.level.isClientSide){
			for(int i = 0; i < 15; i++){
				this.world.spawnParticle(ParticleTypes.SMOKE_LARGE, this.getX() + this.random.nextFloat() - 0.5f,
						this.getY() + this.random.nextFloat() * height, this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	public void onSuccessfulAttack(LivingEntity target){
		target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
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
		// Returns true unless the given entity type is a flying entity and this skeleton does not have a bow.
		return !EntityFlying.class.isAssignableFrom(entityType) || this.getMainHandItem().getItem() instanceof ItemBow;
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
