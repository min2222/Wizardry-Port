package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityLightningSigil extends EntityScaledConstruct {

	public static final String SECONDARY_RANGE = "secondary_range";
	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

	public EntityLightningSigil(Level world){
		super(world);
		setSize(Spells.frost_sigil.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.tickCount > 600 && this.getCaster() == null && !this.level.isClientSide){
			this.discard();
		}

		List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.width/2, this.getX(), this.getY(),
				this.getZ(), this.getBbHeight(), this.world);

		for(LivingEntity target : targets){

			if(this.isValidTarget(target)){

				double velX = target.motionX;
				double velY = target.motionY;
				double velZ = target.motionZ;

				// Only works if target is actually damaged to account for hurtResistantTime
				if(target.hurt(getCaster() != null ? MagicDamage.causeIndirectMagicDamage(this, getCaster(),
						DamageType.SHOCK) : DamageSource.MAGIC, Spells.lightning_sigil.getProperty(Spell.DIRECT_DAMAGE)
						.floatValue() * damageMultiplier)){

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					this.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0f, 1.0f);

					// Secondary chaining effect
					double seekerRange = Spells.lightning_sigil.getProperty(SECONDARY_RANGE).doubleValue();

					List<LivingEntity> secondaryTargets = EntityUtils.getLivingWithinRadius(seekerRange,
							target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), world);

					for(int j = 0; j < Math.min(secondaryTargets.size(),
							Spells.lightning_sigil.getProperty(SECONDARY_MAX_TARGETS).floatValue()); j++){

						LivingEntity secondaryTarget = secondaryTargets.get(j);

						if(secondaryTarget != target && this.isValidTarget(secondaryTarget)){

							if(level.isClientSide){
								
								ParticleBuilder.create(Type.LIGHTNING).entity(target)
								.pos(0, target.getBbHeight()/2, 0).target(secondaryTarget).spawn(world);
								
								ParticleBuilder.spawnShockParticles(world, secondaryTarget.getX(),
										secondaryTarget.getY() + secondaryTarget.getBbHeight() / 2,
										secondaryTarget.getZ());
							}

							secondaryTarget.playSound(WizardrySounds.ENTITY_LIGHTNING_SIGIL_TRIGGER, 1.0F,
									world.random.nextFloat() * 0.4F + 1.5F);

							secondaryTarget.hurt(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK),
									Spells.lightning_sigil.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
						}

					}
					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}

		if(this.level.isClientSide && this.random.nextInt(15) == 0){
			double radius = (0.5 + random.nextDouble() * 0.3) * width/2;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			ParticleBuilder.create(Type.SPARK)
			.pos(this.getX() + radius * Mth.cos(angle), this.getY() + 0.1, this.getZ() + radius * Mth.sin(angle))
			.spawn(world);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
