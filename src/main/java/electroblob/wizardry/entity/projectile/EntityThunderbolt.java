package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntityThunderbolt extends EntityMagicProjectile {

	public static final String KNOCKBACK_STRENGTH = "knockback_strength";

	public EntityThunderbolt(Level par1World){
		super(par1World);
	}

	@Override public boolean hasNoGravity(){ return true; }

	@Override public boolean canRenderOnFire(){ return false; }
	
	@Override
	protected void onImpact(HitResult par1RayTraceResult){
		
		Entity entityHit = par1RayTraceResult.entityHit;

		if(entityHit != null){

			float damage = Spells.thunderbolt.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
					damage);

			float knockbackStrength = Spells.thunderbolt.getProperty(KNOCKBACK_STRENGTH).floatValue();

			// Knockback
			entityHit.addVelocity(this.motionX * knockbackStrength, this.motionY * knockbackStrength, this.motionZ * knockbackStrength);
		}

		this.playSound(WizardrySounds.ENTITY_THUNDERBOLT_HIT, 1.4F, 0.5f + this.random.nextFloat() * 0.1F);

		// Particle effect
		if(level.isClientSide){
			world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		this.discard();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(level.isClientSide){
			ParticleBuilder.create(Type.SPARK, rand, getX(), getY() + height/2, getZ(), 0.1, false).spawn(world);
			for(int i = 0; i < 4; i++){
				world.spawnParticle(ParticleTypes.SMOKE_NORMAL, this.getX() + random.nextFloat() * 0.2 - 0.1,
						this.getY() + this.getBbHeight() / 2 + random.nextFloat() * 0.2 - 0.1,
						this.getZ() + random.nextFloat() * 0.2 - 0.1, 0, 0, 0);
			}
		}
	}

	@Override
	public int getLifetime(){
		return 8;
	}

}
