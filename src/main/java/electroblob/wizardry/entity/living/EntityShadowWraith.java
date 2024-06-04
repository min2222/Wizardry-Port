package electroblob.wizardry.entity.living;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

public class EntityShadowWraith extends EntitySummonedCreature implements ISpellCaster {

	// TODO: This currently doesn't fly like it used to. Should it, or does it not matter?

	private double AISpeed = 1.0;

	private EntityAIAttackSpell<EntityShadowWraith> spellAttackAI = new EntityAIAttackSpell<EntityShadowWraith>(this, AISpeed, 15f, 30, 0);

	private static final List<Spell> attack = Collections.singletonList(Spells.darkness_orb);

	/** Creates a new shadow wraith in the gievn world. */
	public EntityShadowWraith(Level world){
		super(world);
		// For some reason this can't be in initEntityAI
		this.tasks.addTask(0, this.spellAttackAI);
	}

	@Override
	protected void initEntityAI(){

		this.tasks.addTask(1, new EntityAIAttackMelee(this, AISpeed, false));
		this.tasks.addTask(2, new EntityAIWander(this, AISpeed));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, LivingEntity.class,
				0, false, true, this.getTargetSelector()));

		this.setAIMoveSpeed((float)AISpeed);
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
	public boolean isPotionApplicable(MobEffectInstance potion){
		return potion.getPotion() == MobEffects.WITHER ? false : super.isPotionApplicable(potion);
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_SHADOW_WRAITH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_SHADOW_WRAITH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_SHADOW_WRAITH_DEATH;
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
		if(this.level.isClientSide){
			for(int i = 0; i < 15; i++){
				float brightness = random.nextFloat() * 0.4f;
				ParticleBuilder.create(Type.SPARKLE, this).vel(0, 0.05, 0).time(20 + random.nextInt(10))
				.clr(brightness, 0.0f, brightness).spawn(world);
			}
		}
	}

	@Override
	public void onLivingUpdate(){

		if(this.random.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_SHADOW_WRAITH_NOISE, 1.0F + this.random.nextFloat(),
					this.random.nextFloat() * 0.7F + 0.3F);
		}

		// Slow fall
		if(!this.onGround && this.motionY < 0.0D){
			this.motionY *= 0.6D;
		}

		if(level.isClientSide){
			
			for(int i=0; i<2; i++){
				
				world.spawnParticle(ParticleTypes.PORTAL,
						this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.width,
						this.getY() + this.random.nextDouble() * (double)this.getBbHeight(),
						this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0);
				
				world.spawnParticle(ParticleTypes.SMOKE_LARGE,
						this.getX() + (this.random.nextDouble() - 0.5D) * (double)this.width,
						this.getY() + this.random.nextDouble() * (double)this.getBbHeight(),
						this.getZ() + (this.random.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0);
				
				float brightness = random.nextFloat() * 0.2f;
				
				ParticleBuilder.create(Type.SPARKLE, this).vel(0, 0.05, 0).time(20 + random.nextInt(10))
				.clr(brightness, 0.0f, brightness).spawn(world);
				
				ParticleBuilder.create(Type.DARK_MAGIC, this).clr(0.1f, 0.0f, 0.0f).spawn(world);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public void fall(float distance, float damageMultiplier){
		// Immune to fall damage.
	}

}
