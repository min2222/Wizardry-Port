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

		this.playSound(WizardrySounds.ENTITY_THUNDERBOLT_HIT, 1.4F, 0.5f + this.rand.nextFloat() * 0.1F);

		// Particle effect
		if(world.isRemote){
			world.spawnParticle(ParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		this.setDead();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){
			ParticleBuilder.create(Type.SPARK, rand, posX, posY + height/2, posZ, 0.1, false).spawn(world);
			for(int i = 0; i < 4; i++){
				world.spawnParticle(ParticleTypes.SMOKE_NORMAL, this.posX + rand.nextFloat() * 0.2 - 0.1,
						this.posY + this.height / 2 + rand.nextFloat() * 0.2 - 0.1,
						this.posZ + rand.nextFloat() * 0.2 - 0.1, 0, 0, 0);
			}
		}
	}

	@Override
	public int getLifetime(){
		return 8;
	}

}
