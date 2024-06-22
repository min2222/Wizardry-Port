package electroblob.wizardry.entity.living;

import java.util.Calendar;
import java.util.UUID;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class EntityWitherSkeletonMinion extends WitherSkeleton implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerUUID(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new wither skeleton minion in the given world. */
	public EntityWitherSkeletonMinion(Level world){
		this(WizardryEntities.WITHER_SKELETON_MINION.get(), world);
		this.xpReward = 0;
	}
	
	public EntityWitherSkeletonMinion(EntityType<? extends WitherSkeleton> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	// EntitySkeleton overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void registerGoals(){
		super.registerGoals();
		this.targetSelector.removeAllGoals();
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	// Shouldn't have randomised armour, but does still need a sword!
	@Override
	protected void populateDefaultEquipmentSlots(RandomSource source, DifficultyInstance difficulty){
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34178_, DifficultyInstance p_34179_,
			MobSpawnType p_34180_, SpawnGroupData p_34181_, CompoundTag p_34182_){
		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getAttribute(Attributes.FOLLOW_RANGE)
				.addTransientModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, EntityUtils.Operations.MULTIPLY_FLAT));

		if(this.random.nextFloat() < 0.05F){
			this.setLeftHanded(true);
		}else{
			this.setLeftHanded(false);
		}
		
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
		this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);

		// Halloween pumpkin heads! Why not?
		if(this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()){
			Calendar calendar = Calendar.getInstance();

			if(calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F){
				this.setItemSlot(EquipmentSlot.HEAD,
						new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.PUMPKIN));
				this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
			}
		}

		return super.finalizeSpawn(p_34178_, p_34179_, p_34180_, p_34181_, p_34182_);
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

	private void spawnParticleEffect(){
		if(this.level.isClientSide){
			for(int i = 0; i < 15; i++){
				this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX() + this.random.nextFloat() - 0.5f,
						this.getY() + this.random.nextFloat() * getBbHeight(), this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
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
		// Returns true unless the given entity type is a flying entity and this skeleton does not have a bow.
		return !(entityType instanceof FlyingMob) || this.getMainHandItem().getItem() instanceof BowItem;
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
