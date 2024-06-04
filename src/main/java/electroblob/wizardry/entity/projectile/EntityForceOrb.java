package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntityForceOrb extends EntityBomb {
	
	public EntityForceOrb(Level world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(HitResult par1RayTraceResult){

		if(par1RayTraceResult.entityHit != null){
			// This is if the force orb gets a direct hit
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		}

		// Particle effect
		if(this.world.isRemote){
			for(int j = 0; j < 20; j++){
				float brightness = 0.5f + (rand.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE, rand, posX, posY, posZ, 0.25, true).time(6)
				.clr(brightness, 1.0f, brightness + 0.2f).spawn(world);
			}
			this.world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		if(!this.world.isRemote){

			// 2 gives a cool flanging effect!
			float pitch = this.rand.nextFloat() * 0.2F + 0.3F;
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT_BLOCK, 1.5F, pitch);
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT_BLOCK, 1.5F, pitch - 0.01f);

			double blastRadius = Spells.force_orb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(blastRadius, this.posX,
					this.posY, this.posZ, this.world);

			for(LivingEntity target : targets){
				if(target != this.getThrower()){

					double velY = target.motionY;

					double dx = this.posX - target.posX > 0 ? -0.5 - (this.posX - target.posX) / 8
							: 0.5 - (this.posX - target.posX) / 8;
					double dz = this.posZ - target.posZ > 0 ? -0.5 - (this.posZ - target.posZ) / 8
							: 0.5 - (this.posZ - target.posZ) / 8;

					float damage = Spells.force_orb.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.BLAST), damage);

					target.motionX = dx;
					target.motionY = velY + 0.4;
					target.motionZ = dz;
				}
			}

			this.setDead();
		}
	}
	
}
