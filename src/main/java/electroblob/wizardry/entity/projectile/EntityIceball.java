package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityIceball extends EntityMagicProjectile {

	public EntityIceball(Level world){
		this(WizardryEntities.ICEBALL.get(), world);
	}
	
	public EntityIceball(EntityType<? extends EntityMagicProjectile> type, Level world){
		super(type, world);
	}

	@Override
	protected void onHit(HitResult rayTrace){

		if(!level.isClientSide){

			Entity entityHit = rayTrace.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) rayTrace).getEntity() : null;

			if(entityHit != null){

				float damage = Spells.iceball.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

				entityHit.hurt(
						MagicDamage.causeIndirectMagicDamage(this, this.getOwner(), DamageType.FROST).setProjectile(),
						damage);

				if(entityHit instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit)){
					((LivingEntity)entityHit).addEffect(new MobEffectInstance(WizardryPotions.FROST.get(),
							Spells.iceball.getProperty(Spell.EFFECT_DURATION).intValue(),
							Spells.iceball.getProperty(Spell.EFFECT_STRENGTH).intValue()));
				}

			}else{

				BlockPos pos = ((BlockHitResult) rayTrace).getBlockPos();

				if(((BlockHitResult) rayTrace).getDirection() == Direction.UP && !level.isClientSide && level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP)
						&& BlockUtils.canBlockBeReplaced(level, pos.above()) && BlockUtils.canPlaceBlock(getOwner(), level, pos)){
					level.setBlockAndUpdate(pos.above(), Blocks.SNOW.defaultBlockState());
				}
			}

			this.playSound(WizardrySounds.ENTITY_ICEBALL_HIT, 2, 0.8f + random.nextFloat() * 0.3f);

			this.discard();
		}
	}

	@Override
	public void tick(){

		super.tick();

		if(level.isClientSide){

			for(int i=0; i<5; i++){

				double dx = (random.nextDouble() - 0.5) * getBbWidth();
				double dy = (random.nextDouble() - 0.5) * getBbHeight() + this.getBbHeight()/2;
				double dz = (random.nextDouble() - 0.5) * getBbWidth();
				double v = 0.06;
				ParticleBuilder.create(ParticleBuilder.Type.SNOW)
						.pos(this.position().add(dx - this.getDeltaMovement().x/2, dy, dz - this.getDeltaMovement().z/2))
						.vel(-v * dx, -v * dy, -v * dz).scale(getBbWidth()*2).time(8 + random.nextInt(4)).spawn(level);

				if(tickCount > 1){
					dx = (random.nextDouble() - 0.5) * getBbWidth();
					dy = (random.nextDouble() - 0.5) * getBbHeight() + this.getBbHeight() / 2;
					dz = (random.nextDouble() - 0.5) * getBbWidth();
					ParticleBuilder.create(ParticleBuilder.Type.SNOW)
							.pos(this.position().add(dx - this.getDeltaMovement().x, dy, dz - this.getDeltaMovement().z))
							.vel(-v * dx, -v * dy, -v * dz).scale(getBbWidth()*2).time(8 + random.nextInt(4)).spawn(level);
				}
			}
		}
	}

	@Override
	public int getLifetime(){
		return 16;
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
