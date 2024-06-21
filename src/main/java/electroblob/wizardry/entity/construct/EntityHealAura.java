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

public class EntityHealAura extends EntityScaledConstruct {

	public EntityHealAura(Level world){
		this(WizardryEntities.HEALING_AURA.get(), world);
		setSize(Spells.HEALING_AURA.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 1);
	}
	
	public EntityHealAura(EntityType<? extends EntityScaledConstruct> type, Level world){
		super(type, world);
		setSize(Spells.HEALING_AURA.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 1);
	}

	@Override
	public void tick(){

		if(this.tickCount % 25 == 1){
			this.playSound(WizardrySounds.ENTITY_HEAL_AURA_AMBIENT, 0.1f, 1.0f);
		}

		super.tick();

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(getBbWidth()/2, getX(), getY(), getZ(), this.getBbHeight(), level);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					if(target.isInvertedHealAndHarm()) {

						Vec3 velocity = target.getDeltaMovement();

						if (this.tickCount % 10 == 1) {
							if (this.getCaster() != null) {
								target.hurt(
										MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.RADIANT),
										Spells.HEALING_AURA.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
							} else {
								target.hurt(DamageSource.MAGIC, Spells.HEALING_AURA.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
							}

							// Removes knockback
							target.setDeltaMovement(velocity);
						}
					}

				}else if(target.getHealth() < target.getMaxHealth() && target.tickCount % 5 == 0){
					target.heal(Spells.HEALING_AURA.getProperty(Spell.HEALTH).floatValue() * damageMultiplier);
				}
			}
		}else{
			for(int i=1; i<3; i++){
				float brightness = 0.5f + (random.nextFloat() * 0.5f);
				double radius = random.nextDouble() * (getBbWidth()/2);
				float angle = random.nextFloat() * (float)Math.PI * 2;
				ParticleBuilder.create(Type.SPARKLE)
				.pos(this.getX() + radius * Mth.cos(angle), this.getY(), this.getZ() + radius * Mth.sin(angle))
				.vel(0, 0.05, 0)
				.time(48 + this.random.nextInt(12))
				.clr(1.0f, 1.0f, brightness)
				.spawn(level);
			}
		}
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}

}
