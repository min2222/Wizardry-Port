package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// TODO: Try to collect sigils into one superclass
public class EntityFireSigil extends EntityScaledConstruct {

	public EntityFireSigil(Level world){
		this(WizardryEntities.FIRE_SIGIL.get(), world);
		setSize(Spells.fire_sigil.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}
	
	public EntityFireSigil(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
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

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(this.getBbWidth()/2, this.getX(), this.getY(), this.getZ(), this.getBbHeight(), this.level);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					Vec3 velocity = target.getDeltaMovement();

					target.hurt(this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FIRE)
							: DamageSource.MAGIC, Spells.fire_sigil.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					// Removes knockback
					target.setDeltaMovement(velocity);

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
						target.setSecondsOnFire(Spells.fire_sigil.getProperty(Spell.BURN_DURATION).intValue());

					this.playSound(WizardrySounds.ENTITY_FIRE_SIGIL_TRIGGER, 1, 1);

					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}else if(this.random.nextInt(15) == 0){
			double radius = (0.5 + random.nextDouble() * 0.3) * getBbWidth()/2;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			level.addParticle(ParticleTypes.FLAME, this.getX() + radius * Mth.cos(angle), this.getY() + 0.1,
					this.getZ() + radius * Mth.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

}
