package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityThunderbolt extends EntityMagicProjectile {

	public static final String KNOCKBACK_STRENGTH = "knockback_strength";

	public EntityThunderbolt(Level par1World){
		this(WizardryEntities.THUNDERBOLT.get(), par1World);
	}
	
	public EntityThunderbolt(EntityType<? extends EntityMagicProjectile> type, Level par1World){
		super(type, par1World);
	}

	@Override public boolean isNoGravity(){ return true; }

	@Override public boolean displayFireAnimation(){ return false; }
	
	@Override
	protected void onHit(HitResult par1RayTraceResult){
		
		Entity entityHit = par1RayTraceResult.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) par1RayTraceResult).getEntity() : null;

		if(entityHit != null){

			float damage = Spells.thunderbolt.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.SHOCK).setProjectile(),
					damage);

			float knockbackStrength = Spells.thunderbolt.getProperty(KNOCKBACK_STRENGTH).floatValue();

			// Knockback
			entityHit.push(this.getDeltaMovement().x * knockbackStrength, this.getDeltaMovement().y * knockbackStrength, this.getDeltaMovement().z * knockbackStrength);
		}

		this.playSound(WizardrySounds.ENTITY_THUNDERBOLT_HIT, 1.4F, 0.5f + this.random.nextFloat() * 0.1F);

		// Particle effect
		if(level.isClientSide){
			level.addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
		}

		this.discard();
	}

	@Override
	public void tick(){

		super.tick();

		if(level.isClientSide){
			ParticleBuilder.create(Type.SPARK, random, getX(), getY() + getBbHeight()/2, getZ(), 0.1, false).spawn(level);
			for(int i = 0; i < 4; i++){
				level.addParticle(ParticleTypes.SMOKE, this.getX() + random.nextFloat() * 0.2 - 0.1,
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
