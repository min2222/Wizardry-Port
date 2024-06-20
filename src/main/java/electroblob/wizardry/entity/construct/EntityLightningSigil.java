package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityLightningSigil extends EntityScaledConstruct {

	public static final String SECONDARY_RANGE = "secondary_range";
	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntityLightningSigil(Level world){
		this(WizardryEntities.LIGHTNING_SIGIL.get(), world);
		setSize(Spells.LIGHTNING_SIGIL.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}
	
	public EntityLightningSigil(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.LIGHTNING_SIGIL.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void tick(){

		super.tick();

		if(this.tickCount > 600 && this.getCaster() == null && !this.level.isClientSide){
			this.discard();
		}

		List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.getBbWidth()/2, this.getX(), this.getY(),
				this.getZ(), this.getBbHeight(), this.level);

		for(LivingEntity target : targets){

			if(this.isValidTarget(target)){

				Vec3 velocity = target.getDeltaMovement();

				// Only works if target is actually damaged to account for hurtResistantTime
				if(target.hurt(getCaster() != null ? MagicDamage.causeIndirectMagicDamage(this, getCaster(),
						DamageType.SHOCK) : DamageSource.MAGIC, Spells.LIGHTNING_SIGIL.getProperty(Spell.DIRECT_DAMAGE)
						.floatValue() * damageMultiplier)){

					// Removes knockback
					target.setDeltaMovement(velocity);

					this.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0f, 1.0f);

					// Secondary chaining effect
					double seekerRange = Spells.LIGHTNING_SIGIL.getProperty(SECONDARY_RANGE).doubleValue();

					List<LivingEntity> secondaryTargets = EntityUtils.getLivingWithinRadius(seekerRange,
							target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), level);

					for(int j = 0; j < Math.min(secondaryTargets.size(),
							Spells.LIGHTNING_SIGIL.getProperty(SECONDARY_MAX_TARGETS).floatValue()); j++){

						LivingEntity secondaryTarget = secondaryTargets.get(j);

						if(secondaryTarget != target && this.isValidTarget(secondaryTarget)){

							if(level.isClientSide){
								
								ParticleBuilder.create(Type.LIGHTNING).entity(target)
								.pos(0, target.getBbHeight()/2, 0).target(secondaryTarget).spawn(level);
								
								ParticleBuilder.spawnShockParticles(level, secondaryTarget.getX(),
										secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2,
										secondaryTarget.getZ());
							}

							secondaryTarget.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0F,
									level.random.nextFloat() * 0.4F + 1.5F);

							secondaryTarget.hurt(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK),
									Spells.LIGHTNING_SIGIL.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
						}

					}
					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}

		if(this.level.isClientSide && this.random.nextInt(15) == 0){
			double radius = (0.5 + random.nextDouble() * 0.3) * getBbWidth()/2;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			ParticleBuilder.create(Type.SPARK)
			.pos(this.getX() + radius * Mth.cos(angle), this.getY() + 0.1, this.getZ() + radius * Mth.sin(angle))
			.spawn(level);
		}
	}

	@Override
	protected void defineSynchedData(){}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

}
