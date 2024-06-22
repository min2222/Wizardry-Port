package electroblob.wizardry.entity.living;

import java.util.Collections;
import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;

public class EntityStormElemental extends EntitySummonedCreature implements ISpellCaster {

	private double AISpeed = 1.0;

	private EntityAIAttackSpell<EntityStormElemental> spellAttackAI = new EntityAIAttackSpell<EntityStormElemental>(this, AISpeed, 15f, 30, 0);

	private static final List<Spell> attack = Collections.singletonList(Spells.LIGHTNING_DISC);

	/** Creates a new storm elemental in the given world. */
	public EntityStormElemental(Level world){
		this(WizardryEntities.STORM_ELEMENTAL.get(), world);
		// For some reason this can't be in initEntityAI
		this.goalSelector.addGoal(0, this.spellAttackAI);
	}
	
	public EntityStormElemental(EntityType<? extends EntitySummonedCreature> type, Level world){
		super(type, world);
		// For some reason this can't be in initEntityAI
		this.goalSelector.addGoal(0, this.spellAttackAI);
	}

	@Override
	protected void registerGoals(){

		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, AISpeed, false));
		this.goalSelector.addGoal(2, new RandomStrollGoal(this, AISpeed));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<LivingEntity>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));

		this.setSpeed((float)AISpeed);
	}

	@Override
	public boolean hasRangedAttack(){
		return true;
	}

	@Override
	public List<Spell> getSpells(){
		return attack;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		this.getEntityAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(AISpeed);
		this.getEntityAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getEntityAttribute(Attributes.FOLLOW_RANGE).setBaseValue(16.0D);
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_DEATH;
	}

	@Override
	public void aiStep(){

		if(this.tickCount % 120 == 1){
			this.playSound(WizardrySounds.ENTITY_STORM_ELEMENTAL_WIND, 1.0f, 1.0f);
		}

		if(this.random.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_STORM_ELEMENTAL_BURN, 1.0F + this.random.nextFloat(),
					this.random.nextFloat() * 0.7F + 0.3F);
		}

		// Slow fall
		if(!this.onGround && this.getDeltaMovement().y < 0.0D){
			this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.6D, 1));
		}

		if(level.isClientSide){

			for(int i=0; i<2; ++i){
				
				level.addParticle(ParticleTypes.LARGE_SMOKE,
						this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(),
						this.getY() + this.random.nextDouble() * (double)this.getBbHeight(),
						this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.getBbWidth(), 0, 0, 0);
				
				ParticleBuilder.create(Type.SPARK, this).spawn(level);
			}

			for(int i=0; i<10; i++){
				
				float brightness = random.nextFloat() * 0.2f;
				double dy = this.random.nextDouble() * (double)this.getBbHeight();
				
				ParticleBuilder.create(Type.SPARKLE).pos(this.getX(), this.getY() + dy, this.getZ())
				.time(20 + random.nextInt(10)).clr(0, brightness, brightness)//.entity(this)
				.spin(0.2 + 0.5 * dy, 0.1 + 0.05 * level.random.nextDouble()).spawn(level);
			}
		}

		super.aiStep();
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source){
		// Immune to fall damage.
		return false;
	}

	@Override
	public void thunderHit(ServerLevel level, LightningBolt lightning){
		// Immune to lightning.
	}
}