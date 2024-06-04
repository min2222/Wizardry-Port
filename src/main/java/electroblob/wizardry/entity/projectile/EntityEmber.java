package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Disintegration;
import electroblob.wizardry.spell.Spell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntityEmber extends EntityMagicProjectile {

	private int extraLifetime;

	public EntityEmber(Level world){
		super(world);
	}

	public EntityEmber(Level world, LivingEntity caster){
		super(world);
		this.thrower = caster;
		extraLifetime = random.nextInt(30);
		this.setSize(0.1f, 0.1f);
	}

	@Override
	public AABB getCollisionBoundingBox(){
		return null;//this.getBoundingBox();
	}

	@Override
	public int getLifetime(){
		return Spells.disintegration.getProperty(Disintegration.EMBER_LIFETIME).intValue() + extraLifetime;
	}

	@Override
	protected void onImpact(HitResult result){

//		if(result.entityHit != null){
//			result.entityHit.setSecondsOnFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue());
//		}

		if(result.typeOfHit == HitResult.Type.BLOCK){
			this.inGround = true;
			this.collided = true;
			if(result.sideHit.getAxis() == Direction.Axis.X) motionX = 0;
			if(result.sideHit.getAxis() == Direction.Axis.Y){
				motionY = 0;
				this.collidedVertically = true;
			}
			if(result.sideHit.getAxis() == Direction.Axis.Z) motionZ = 0;
		}
	}

	@Override
	public void applyEntityCollision(Entity entity){

		super.applyEntityCollision(entity);

		if(entity instanceof LivingEntity && ((LivingEntity)entity).getHealth() > 0){
			entity.setSecondsOnFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue());
		}
	}

	@Override
	public void tick(){

		super.tick();

		if(this.collidedVertically){
			this.motionY += this.getGravityVelocity();
			this.motionX *= 0.5;
			this.motionZ *= 0.5;
		}

		level.getEntitiesInAABBexcluding(thrower, this.getBoundingBox(), e -> e instanceof LivingEntity)
				.stream().filter(e -> !(e instanceof LivingEntity) || ((LivingEntity)e).getHealth() > 0)
				.forEach(e -> e.setSecondsOnFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue()));

		// Copied from ParticleLava
		if(this.random.nextFloat() > (float)this.tickCount / this.getLifetime()){
			this.world.spawnParticle(ParticleTypes.SMOKE_NORMAL, this.getX(), this.getY(), this.getZ(), this.motionX, this.motionY, this.motionZ);
		}
	}
}
