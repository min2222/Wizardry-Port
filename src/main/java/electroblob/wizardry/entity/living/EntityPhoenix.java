package electroblob.wizardry.entity.living;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.EntityAIHurtByTarget;
import net.minecraft.world.entity.ai.EntityAILookIdle;
import net.minecraft.world.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.world.entity.ai.EntityAIWatchClosest;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

public class EntityPhoenix extends EntitySummonedCreature implements ISpellCaster {

	private double AISpeed = 0.5;

	// Can attack for 7 seconds, then must cool down for 3.
	private EntityAIAttackSpell<EntityPhoenix> spellAttackAI = new EntityAIAttackSpell<>(this, AISpeed, 15f, 60, 140);

	private Spell continuousSpell;
	private int spellCounter;

	private static final List<Spell> attack = Collections.singletonList(Spells.flame_ray);

	/** Creates a new phoenix in the given world. */
	public EntityPhoenix(Level world){
		super(world);
		this.isImmuneToFire = true;
		this.getBbHeight() = 2.0f;
		// For some reason this can't be in initEntityAI
		this.tasks.addTask(1, this.spellAttackAI);
	}

	@Override
	protected void initEntityAI(){

		this.tasks.addTask(0, new EntityAIWatchClosest(this, LivingEntity.class, 0));
		// this.tasks.addTask(2, new EntityAIWander(this, AISpeed));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		// this.targetTasks.addTask(0, new EntityAIMoveTowardsTarget(this, 1, 10));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));

		this.setAIMoveSpeed((float)AISpeed);
	}

	@Override
	public List<Spell> getSpells(){
		return attack;
	}

	@Override
	public SpellModifiers getModifiers(){
		return new SpellModifiers();
	}

	@Override
	public Spell getContinuousSpell(){
		return continuousSpell;
	}

	@Override
	public void setContinuousSpell(Spell spell){
		continuousSpell = spell;
	}

	@Override
	public void setSpellCounter(int count){
		spellCounter = count;
	}

	@Override
	public int getSpellCounter(){
		return spellCounter;
	}

	@Override
	public boolean hasRangedAttack(){
		return true;
	}

	@Override
	// Makes the flames come from the phoenix's head rather than its body
	public float getEyeHeight(){
		return 2.1f;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		this.getEntityAttribute(Attributes.FOLLOW_RANGE).setBaseValue(30.0D);
		this.getEntityAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_PHOENIX_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_PHOENIX_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_PHOENIX_DEATH;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBrightnessForRender(){
		return 15728880;
	}

	@Override
	public float getBrightness(){
		return 1.0F;
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
				this.world.spawnParticle(ParticleTypes.FLAME, this.getX() + this.random.nextFloat() - 0.5f,
						this.getY() + this.random.nextFloat() * height, this.getZ() + this.random.nextFloat() - 0.5f, 0, 0, 0);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffdd4d, 0xff6600, animationProgress);
	}

	@Override
	public void onLivingUpdate(){

		// Makes the phoenix hover.
		Integer floorLevel = BlockUtils.getNearestFloor(world, this.blockPosition(), 4);

		if(floorLevel == null || this.getY() - floorLevel > 3){
			this.motionY = -0.1;
		}else if(this.getY() - floorLevel < 2){
			this.motionY = 0.1;
		}else{
			this.motionY = 0.0;
		}

		// Living sound
		if(this.random.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_PHOENIX_BURN, 1.0F + this.random.nextFloat(),
					this.random.nextFloat() * 0.7F + 0.3F);
		}

		// Flapping sound effect
		if(this.tickCount % 22 == 0){
			this.playSound(WizardrySounds.ENTITY_PHOENIX_FLAP, 1.0F, 1.0f);
		}

		for(int i = 0; i < 2; i++){
			this.world.spawnParticle(ParticleTypes.FLAME,
					this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.width,
					this.getY() + this.getBbHeight() / 2 + this.random.nextDouble() * (double)this.getBbHeight() / 2,
					this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.width, 0.0D, -0.1D, 0.0D);
		}

		// Adding this allows the phoenix to attack despite being in the air. However, for some strange reason
		// it will only attack when within about 3 blocks of the ground. Any higher and it just sits there, not even
		// attempting to find targets.

		this.onGround = true;

		super.onLivingUpdate();
	}

	@Override
	public void fall(float distance, float damageMultiplier){} // Immune to fall damage

	@Override
	public boolean isBurning(){
		return false;
	}
}
