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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EntityForceOrb extends EntityBomb {
	
	public EntityForceOrb(Level world){
		this(WizardryEntities.FORCE_ORB.get(), world);
	}
	
	public EntityForceOrb(EntityType<? extends EntityBomb> type, Level world){
		super(type, world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onHit(HitResult par1RayTraceResult){

		if(par1RayTraceResult.getType() == HitResult.Type.ENTITY){
			// This is if the force orb gets a direct hit
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
		}

		// Particle effect
		if(this.level.isClientSide){
			for(int j = 0; j < 20; j++){
				float brightness = 0.5f + (random.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE, random, getX(), getY(), getZ(), 0.25, true).time(6)
				.clr(brightness, 1.0f, brightness + 0.2f).spawn(level);
			}
			this.level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		if(!this.level.isClientSide){

			// 2 gives a cool flanging effect!
			float pitch = this.random.nextFloat() * 0.2F + 0.3F;
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT_BLOCK, 1.5F, pitch);
			this.playSound(WizardrySounds.ENTITY_FORCE_ORB_HIT_BLOCK, 1.5F, pitch - 0.01f);

			double blastRadius = Spells.force_orb.getProperty(Spell.BLAST_RADIUS).floatValue() * blastMultiplier;

			List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(blastRadius, this.getX(),
					this.getY(), this.getZ(), this.level);

			for(LivingEntity target : targets){
				if(target != this.getOwner()){

					double velY = target.getDeltaMovement().y;

					double dx = this.getX() - target.getX() > 0 ? -0.5 - (this.getX() - target.getX()) / 8
							: 0.5 - (this.getX() - target.getX()) / 8;
					double dz = this.getZ() - target.getZ() > 0 ? -0.5 - (this.getZ() - target.getZ()) / 8
							: 0.5 - (this.getZ() - target.getZ()) / 8;

					float damage = Spells.force_orb.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

					target.hurt(
							MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.BLAST), damage);
					target.setDeltaMovement(dx, velY + 0.4, dz);
				}
			}

			this.discard();
		}
	}
	
}
