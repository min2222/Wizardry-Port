package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityFrostSigil extends EntityScaledConstruct {

	public EntityFrostSigil(Level world){
		this(WizardryEntities.FROST_SIGIL.get(), world);
		setSize(Spells.FROST_SIGIL.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}
	
	public EntityFrostSigil(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.FROST_SIGIL.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 0.2f);
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void tick(){

		super.tick();

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(getBbWidth()/2, this.getX(), this.getY(),
					this.getZ(), this.getBbHeight(), this.level);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){
					
					EntityUtils.attackEntityWithoutKnockback(target, this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FROST)
							: DamageSource.MAGIC, Spells.FROST_SIGIL.getProperty(Spell.DAMAGE).floatValue()
							* damageMultiplier);

					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
								Spells.FROST_SIGIL.getProperty(Spell.EFFECT_DURATION).intValue(),
								Spells.FROST_SIGIL.getProperty(Spell.EFFECT_STRENGTH).intValue()));

					this.playSound(WizardrySounds.ENTITY_FROST_SIGIL_TRIGGER, 1.0f, 1.0f);

					// The trap is destroyed once triggered.
					this.discard();
				}
			}
		}else if(this.random.nextInt(15) == 0){
			double radius = (0.5 + random.nextDouble() * 0.3) * getBbWidth()/2;
			float angle = random.nextFloat() * (float)Math.PI * 2;
			ParticleBuilder.create(Type.SNOW)
			.pos(this.getX() + radius * Mth.cos(angle), this.getY() + 0.1, this.getZ() + radius * Mth.sin(angle))
			.vel(0, 0, 0) // Required since default for snow is not stationary
			.spawn(level);
		}
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

}
