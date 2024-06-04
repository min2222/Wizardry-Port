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
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.width/2, this.posX, this.posY, this.posZ, this.height, this.world);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					target.attackEntityFrom(this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FIRE)
							: DamageSource.MAGIC, Spells.fire_sigil.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
						target.setFire(Spells.fire_sigil.getProperty(Spell.BURN_DURATION).intValue());

					this.playSound(WizardrySounds.ENTITY_FIRE_SIGIL_TRIGGER, 1, 1);

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = (0.5 + rand.nextDouble() * 0.3) * width/2;
			float angle = rand.nextFloat() * (float)Math.PI * 2;
			world.spawnParticle(ParticleTypes.FLAME, this.posX + radius * Mth.cos(angle), this.posY + 0.1,
					this.posZ + radius * Mth.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
