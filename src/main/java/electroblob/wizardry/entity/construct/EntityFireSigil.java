package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

import java.util.List;

// TODO: Try to collect sigils into one superclass
public class EntityFireSigil extends EntityScaledConstruct {

	public EntityFireSigil(Level world){
		super(world);
		setSize(Spells.fire_sigil.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void tick(){

		super.tick();

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.width/2, this.getX(), this.getY(), this.getZ(), this.getBbHeight(), this.world);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					target.hurt(this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FIRE)
							: DamageSource.MAGIC, Spells.fire_sigil.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
						target.setSecondsOnFire(Spells.fire_sigil.getProperty(Spell.BURN_DURATION).intValue());

					this.playSound(WizardrySounds.ENTITY_FIRE_SIGIL_TRIGGER, 1, 1);

					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}else if(this.random.nextInt(15) == 0){
			double radius = (0.5 + random.nextDouble() * 0.3) * width/2;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			world.spawnParticle(ParticleTypes.FLAME, this.getX() + radius * Mth.cos(angle), this.getY() + 0.1,
					this.getZ() + radius * Mth.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
