package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityFirebomb extends EntityBomb {

	public EntityFirebomb(Level world){
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
			// This is if the firebomb gets a direct hit
			float damage = Spells.firebomb.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit))
				entityHit.setFire(Spells.firebomb.getProperty(Spell.BURN_DURATION).intValue());
		}

		// Particle effect
		if(level.isClientSide){
			
			ParticleBuilder.create(Type.FLASH).pos(this.getPositionVector()).scale(5 * blastMultiplier).clr(1, 0.6f, 0)
			.spawn(world);

			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				ParticleBuilder.create(Type.MAGIC_FIRE, rand, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.time(10 + random.nextInt(4)).scale(2 + random.nextFloat()).spawn(world);
				
				ParticleBuilder.create(Type.DARK_MAGIC, rand, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.clr(1.0f, 0.2f + random.nextFloat() * 0.4f, 0.0f).spawn(world);
			}

			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_FIREBOMB_SMASH, 1.5F, random.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_FIREBOMB_FIRE, 1, 1);

			double range = Spells.firebomb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(range, this.getX(), this.getY(),
					this.getZ(), this.world);

			for(LivingEntity target : targets){
				if(target != entityHit && target != this.getThrower()
						&& !MagicDamage.isEntityImmune(DamageType.FIRE, target)){
					// Splash damage does not count as projectile damage
					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE),
							Spells.firebomb.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
					target.setFire(Spells.firebomb.getProperty(Spell.BURN_DURATION).intValue());
				}
			}

			this.discard();
		}
	}

}
