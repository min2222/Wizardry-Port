package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityHealAura extends EntityScaledConstruct {

	public EntityHealAura(Level world){
		super(world);
		setSize(Spells.healing_aura.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2, 1);
	}

	@Override
	public void onUpdate(){

		if(this.tickCount % 25 == 1){
			this.playSound(WizardrySounds.ENTITY_HEAL_AURA_AMBIENT, 0.1f, 1.0f);
		}

		super.onUpdate();

		if(!this.level.isClientSide){

			List<LivingEntity> targets = EntityUtils.getLivingWithinCylinder(width/2, getX(), getY(), getZ(), this.getBbHeight(), world);

			for(LivingEntity target : targets){

				if(this.isValidTarget(target)){

					if(target.isEntityUndead()) {

						double velX = target.motionX;
						double velY = target.motionY;
						double velZ = target.motionZ;

						if (this.tickCount % 10 == 1) {
							if (this.getCaster() != null) {
								target.hurt(
										MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.RADIANT),
										Spells.healing_aura.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
							} else {
								target.hurt(DamageSource.MAGIC, Spells.healing_aura.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier);
							}

							// Removes knockback
							target.motionX = velX;
							target.motionY = velY;
							target.motionZ = velZ;
						}
					}

				}else if(target.getHealth() < target.getMaxHealth() && target.tickCount % 5 == 0){
					target.heal(Spells.healing_aura.getProperty(Spell.HEALTH).floatValue() * damageMultiplier);
				}
			}
		}else{
			for(int i=1; i<3; i++){
				float brightness = 0.5f + (random.nextFloat() * 0.5f);
				double radius = random.nextDouble() * (width/2);
				float angle = random.nextFloat() * (float)Math.PI * 2;
				ParticleBuilder.create(Type.SPARKLE)
				.pos(this.getX() + radius * Mth.cos(angle), this.getY(), this.getZ() + radius * Mth.sin(angle))
				.vel(0, 0.05, 0)
				.time(48 + this.random.nextInt(12))
				.clr(1.0f, 1.0f, brightness)
				.spawn(world);
			}
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
