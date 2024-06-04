package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityBlizzard extends EntityScaledConstruct {

	public EntityBlizzard(Level world){
		super(world);
		// TODO: Set the size properly and do whatever forcefield does to allow block and entity interaction inside it
		// 		 (Probably need to do this for several others too)
		setSize(Spells.blizzard.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 3);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	public void onUpdate(){

		if(this.tickCount % 120 == 1){
			this.playSound(WizardrySounds.ENTITY_BLIZZARD_AMBIENT, 1.0f, 1.0f);
		}

		super.onUpdate();

		// This is a good example of why you might define a spell base property without necessarily using it in the
		// spell - in fact, blizzard doesn't even have a spell class (yet)
		double radius = Spells.blizzard.getProperty(Spell.EFFECT_RADIUS).doubleValue() * sizeMultiplier;

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, this.getX(), this.getY(),
					this.getZ(), this.world);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					if(this.getCaster() != null){
						EntityUtils.attackEntityWithoutKnockback(target,
								MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.FROST),
								1 * damageMultiplier);
					}else{
						EntityUtils.attackEntityWithoutKnockback(target, DamageSource.MAGIC,
								1 * damageMultiplier);
					}
				}

				// All entities are slowed, even the caster (except those immune to frost effects)
				if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
					target.addEffect(new MobEffectInstance(WizardryPotions.frost, 20, 0));
			}
			
		}else{
			
			for(int i=0; i<6; i++){
				double speed = (random.nextBoolean() ? 1 : -1) * (0.1 + 0.05 * random.nextDouble());
				ParticleBuilder.create(Type.SNOW).pos(this.getX(), this.getY() + random.nextDouble() * height, this.getZ()).vel(0, 0, 0)
				.time(100).scale(2).spin(random.nextDouble() * (radius - 0.5) + 0.5, speed).shaded(true).spawn(world);
			}

			for(int i=0; i<3; i++){
				double speed = (random.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * random.nextDouble());
				ParticleBuilder.create(Type.CLOUD).pos(this.getX(), this.getY() + random.nextDouble() * (height - 0.5), this.getZ())
						.clr(0xffffff).shaded(true).spin(random.nextDouble() * (radius - 1) + 0.5, speed).spawn(world);
			}
		}
	}
}
