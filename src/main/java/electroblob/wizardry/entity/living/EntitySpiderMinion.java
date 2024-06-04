package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.EntityFlying;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IEntityLivingData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.EntityAIHurtByTarget;
import net.minecraft.world.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.EntityCaveSpider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntitySpiderMinion extends EntityCaveSpider implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new spider minion in the given world. */
	public EntitySpiderMinion(Level world){
		super(world);
		this.experienceValue = 0;
	}

	// EntitySpider overrides

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		// Spiders use a custom AI type specific to spiders which I can't access, but it's just an extension of
		// EntityAINearestAttackableTarget which takes daylight into account. Since I want spider minions to attack
		// regardless of daylight, I can just use EntityAINearestAttackableTarget.
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<LivingEntity>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));
	}

	// No spider jockeys!
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata){

		// Can't call super, so the code from the next level up (EntityLiving) had to be copied as well.
		this.getEntityAttribute(Attributes.FOLLOW_RANGE)
				.applyModifier(new AttributeModifier("Random spawn bonus", this.random.nextGaussian() * 0.05D, EntityUtils.Operations.MULTIPLY_FLAT));

		if(this.random.nextFloat() < 0.05F){
			this.setLeftHanded(true);
		}else{
			this.setLeftHanded(false);
		}

		// Don't need anything from EntitySpider, since neither spider jockeys nor group data is relevant.
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
				ParticleBuilder.create(Type.DARK_MAGIC)
				.pos(this.getX() + this.random.nextFloat(), this.getY() + this.random.nextFloat(), this.getZ() + this.random.nextFloat())
				.clr(0.1f, 0.2f, 0.0f)
				.spawn(world);
			}
		}
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
	}

	@Override
	public void onSuccessfulAttack(LivingEntity target){

		int seconds = 0;

		if(this.world.getDifficulty() == Difficulty.NORMAL){
			seconds = 7;
		}else if(this.world.getDifficulty() == Difficulty.HARD){
			seconds = 15;
		}

		if(seconds > 0){
			target.addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0));
		}
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
