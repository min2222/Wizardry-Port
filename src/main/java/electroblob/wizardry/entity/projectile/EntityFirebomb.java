package electroblob.wizardry.entity.projectile;

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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityFirebomb extends EntityBomb {

	public EntityFirebomb(Level world){
		this(WizardryEntities.FIREBOMB.get(), world);
	}
	
	public EntityFirebomb(EntityType<? extends EntityBomb> type, Level world){
		super(type, world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onHit(HitResult rayTrace){
		
		Entity entityHit = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

		if(entityHit != null){
			// This is if the firebomb gets a direct hit
			float damage = Spells.FIREBOMB.getProperty(Spell.DIRECT_DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit))
				entityHit.setSecondsOnFire(Spells.FIREBOMB.getProperty(Spell.BURN_DURATION).intValue());
		}

		// Particle effect
		if(level.isClientSide){
			
			ParticleBuilder.create(Type.FLASH).pos(this.position()).scale(5 * blastMultiplier).clr(1, 0.6f, 0)
			.spawn(level);

			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				ParticleBuilder.create(Type.MAGIC_FIRE, random, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.time(10 + random.nextInt(4)).scale(2 + random.nextFloat()).spawn(level);
				
				ParticleBuilder.create(Type.DARK_MAGIC, random, getX(), getY(), getZ(), 2*blastMultiplier, false)
				.clr(1.0f, 0.2f + random.nextFloat() * 0.4f, 0.0f).spawn(level);
			}

			this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		if(!this.level.isClientSide){

			this.playSound(WizardrySounds.ENTITY_FIREBOMB_SMASH, 1.5F, random.nextFloat() * 0.4F + 0.6F);
			this.playSound(WizardrySounds.ENTITY_FIREBOMB_FIRE, 1, 1);

			double range = Spells.FIREBOMB.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(range, this.getX(), this.getY(),
					this.getZ(), this.level);

			for(LivingEntity target : targets){
				if(target != entityHit && target != this.getOwner()
						&& !MagicDamage.isEntityImmune(DamageType.FIRE, target)){
					// Splash damage does not count as projectile damage
					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.FIRE),
							Spells.FIREBOMB.getProperty(Spell.SPLASH_DAMAGE).floatValue() * damageMultiplier);
					target.setSecondsOnFire(Spells.FIREBOMB.getProperty(Spell.BURN_DURATION).intValue());
				}
			}

			this.discard();
		}
	}

}
