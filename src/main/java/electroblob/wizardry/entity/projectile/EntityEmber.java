package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.spell.Disintegration;
import electroblob.wizardry.spell.Spell;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityEmber extends EntityMagicProjectile {

	private int extraLifetime;

	public EntityEmber(EntityType<? extends EntityMagicProjectile> type, Level world){
		super(type, world);
	}

	public EntityEmber(Level world, LivingEntity caster){
		this(WizardryEntities.EMBER.get(), world);
		this.setOwner(caster);
		extraLifetime = random.nextInt(30);
	}

	@Override
	public int getLifetime(){
		return Spells.DISINTEGRATION.getProperty(Disintegration.EMBER_LIFETIME).intValue() + extraLifetime;
	}

	@Override
	protected void onHit(HitResult result){

//		if(result.entityHit != null){
//			result.entityHit.setSecondsOnFire(Spells.disintegration.getProperty(Spell.BURN_DURATION).intValue());
//		}

		if(result.getType() == HitResult.Type.BLOCK){
			this.setOnGround(true);
			if(((BlockHitResult) result).getDirection().getAxis() == Direction.Axis.X) this.setDeltaMovement(0, this.getDeltaMovement().y, this.getDeltaMovement().z);
			if(((BlockHitResult) result).getDirection().getAxis() == Direction.Axis.Y){
				this.setDeltaMovement(this.getDeltaMovement().x, 0, this.getDeltaMovement().z);
				this.verticalCollision = true;
			}
			if(((BlockHitResult) result).getDirection().getAxis() == Direction.Axis.Z) this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y, 0);
		}
	}

	@Override
	public void push(Entity entity){

		super.push(entity);

		if(entity instanceof LivingEntity && ((LivingEntity)entity).getHealth() > 0){
			entity.setSecondsOnFire(Spells.DISINTEGRATION.getProperty(Spell.BURN_DURATION).intValue());
		}
	}

	@Override
	public void tick(){

		super.tick();

		if(this.verticalCollision){
            this.setDeltaMovement(this.getDeltaMovement().x * 0.5, this.getDeltaMovement().y + this.getGravity(), this.getDeltaMovement().z * 0.5);
		}

		level.getEntities(getOwner(), this.getBoundingBox(), e -> e instanceof LivingEntity)
				.stream().filter(e -> !(e instanceof LivingEntity) || ((LivingEntity)e).getHealth() > 0)
				.forEach(e -> e.setSecondsOnFire(Spells.DISINTEGRATION.getProperty(Spell.BURN_DURATION).intValue()));

		// Copied from ParticleLava
		if(this.random.nextFloat() > (float)this.tickCount / this.getLifetime()){
			this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z);
		}
	}
}
