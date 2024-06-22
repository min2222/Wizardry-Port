package electroblob.wizardry.entity.living;

import java.util.UUID;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityVexMinion extends Vex implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerUUID(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new vex minion in the given world. */
	public EntityVexMinion(Level world){
		this(WizardryEntities.VEX_MINION.get(), world);
		this.xpReward = 0;
	}
	
	public EntityVexMinion(EntityType<? extends Vex> type, Level world){
		super(type, world);
		this.xpReward = 0;
	}

	// ISummonedCreature overrides
	@Override
	public void setCaster(@Nullable LivingEntity caster){
		// Integrates the summoned creature caster system with the (subtly different) vex owner system for NPC casters
		ISummonedCreature.super.setCaster(caster);
		if(caster instanceof Mob) this.setOwner((Mob)caster);
	}

	// EntityVex overrides
	@Override
	protected void registerGoals(){
		super.registerGoals();
		this.targetSelector.removeAllGoals();
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
				0, false, false, this.getTargetSelector()));
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
				ParticleBuilder.create(Type.DARK_MAGIC)
				.pos(this.getX() + this.random.nextFloat(), this.getY() + this.random.nextFloat(), this.getZ() + this.random.nextFloat())
				.clr(0.3f, 0.3f, 0.3f)
				.spawn(level);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return 0xef829c;
	}

	@Override
	public boolean hasParticleEffect(){
		return true;
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