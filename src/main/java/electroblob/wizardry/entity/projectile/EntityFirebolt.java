package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityFirebolt extends EntityMagicProjectile {
	
	public EntityFirebolt(Level world){
		this(WizardryEntities.FIREBOLT.get(), world);
	}
	
	public EntityFirebolt(EntityType<? extends EntityMagicProjectile> type, Level world){
		super(type, world);
	}

	@Override
	protected void onHit(HitResult rayTrace){
		
		Entity entityHit = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

		if(entityHit != null){

			float damage = Spells.firebolt.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.hurt(
					MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit))
				entityHit.setSecondsOnFire(Spells.firebolt.getProperty(Spell.BURN_DURATION).intValue());
		}

		this.playSound(WizardrySounds.ENTITY_FIREBOLT_HIT, 2, 0.8f + random.nextFloat() * 0.3f);

		// Particle effect
		if(level.isClientSide){
			for(int i = 0; i < 8; i++){
				level.addParticle(ParticleTypes.LAVA, this.getX() + random.nextFloat() - 0.5,
						this.getY() + this.getBbHeight() / 2 + random.nextFloat() - 0.5, this.getZ() + random.nextFloat() - 0.5, 0, 0, 0);
			}
		}

		this.discard();
	}

	@Override
	public void tick(){

		super.tick();

		if(level.isClientSide){
			ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE, this).time(14).spawn(level);

			if(this.tickCount > 1){ // Don't spawn particles behind where it started!
				double x = getX() - getDeltaMovement().x/2 + random.nextFloat() * 0.2 - 0.1;
				double y = getY() + this.getBbHeight()/2 - getDeltaMovement().y/2 + random.nextFloat() * 0.2 - 0.1;
				double z = getZ() - getDeltaMovement().z/2 + random.nextFloat() * 0.2 - 0.1;
				ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE).pos(x, y, z).time(14).spawn(level);
			}
		}
	}

	@Override
	public int getLifetime(){
		return 6;
	}

	@Override
	public boolean isNoGravity(){
		return true;
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}
}
