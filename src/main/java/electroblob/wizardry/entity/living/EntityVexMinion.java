package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.EntityAIHurtByTarget;
import net.minecraft.world.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.monster.EntityVex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityVexMinion extends EntityVex implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public UUID getOwnerId(){ return casterUUID; }
	@Override public void setOwnerId(UUID uuid){ this.casterUUID = uuid; }

	/** Creates a new vex minion in the given world. */
	public EntityVexMinion(Level world){
		super(world);
		this.experienceValue = 0;
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
	protected void initEntityAI(){
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, LivingEntity.class,
				0, false, false, this.getTargetSelector()));
	}

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
		this.spawnParticleEffect();
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				ParticleBuilder.create(Type.DARK_MAGIC)
				.pos(this.posX + this.rand.nextFloat(), this.posY + this.rand.nextFloat(), this.posZ + this.rand.nextFloat())
				.clr(0.3f, 0.3f, 0.3f)
				.spawn(world);
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