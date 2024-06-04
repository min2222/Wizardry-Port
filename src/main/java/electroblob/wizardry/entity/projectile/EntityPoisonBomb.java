package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityPoisonBomb extends EntityBomb {

	public EntityPoisonBomb(Level world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(HitResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the poison bomb gets a direct hit
			float damage = Spells.poison_bomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON).setProjectile(),
					damage);

			if(entityHit instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.POISON, entityHit))
				((LivingEntity)entityHit).addEffect(new MobEffectInstance(MobEffects.POISON,
						Spells.poison_bomb.getProperty(Spell.DIRECT_EFFECT_DURATION).intValue(),
						Spells.poison_bomb.getProperty(Spell.DIRECT_EFFECT_STRENGTH).intValue()));
		}

		// Particle effect
		if(level.isClientSide){
			
			ParticleBuilder.create(Type.FLASH).pos(this.getPositionVector()).scale(5 * blastMultiplier)
			.clr(0.2f + random.nextFloat() * 0.3f, 0.6f, 0.0f).spawn(world);
			
			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				ParticleBuilder.create(Type.SPARKLE, rand, getX(), getY(), getZ(), 2*blastMultiplier, false).time(35)
				.scale(2).clr(0.2f + random.nextFloat() * 0.3f, 0.6f, 0.0f).spawn(world);
				
				ParticleBuilder.create(Type.DARK_MAGIC, rand, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.clr(0.2f + random.nextFloat() * 0.2f, 0.8f, 0.0f).spawn(world);
			}
			// Spawning this after the other particles fixes the rendering colour bug. It's a bit of a cheat, but it
			// works pretty well.
			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_SMASH, 1.5F, random.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_POISON_BOMB_POISON, 1.2F, 1.0f);

			double range = Spells.poison_bomb.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(range, this.getX(), this.getY(),
					this.getZ(), this.world);

			for(LivingEntity target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.POISON, target)){
					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.POISON),
							Spells.poison_bomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
					target.addEffect(new MobEffectInstance(MobEffects.POISON,
							Spells.poison_bomb.getProperty(Spell.SPLASH_EFFECT_DURATION).intValue(),
							Spells.poison_bomb.getProperty(Spell.SPLASH_EFFECT_STRENGTH).intValue()));
				}
			}

			this.discard();
		}
	}
}
