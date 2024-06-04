package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntityIceball extends EntityMagicProjectile {

	public EntityIceball(Level world){
		super(world);
		this.setSize(0.5f, 0.5f);
	}

	@Override
	protected void onImpact(HitResult rayTrace){

		if(!world.isRemote){

			Entity entityHit = rayTrace.entityHit;

			if(entityHit != null){

				float damage = Spells.iceball.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

				entityHit.attackEntityFrom(
						MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FROST).setProjectile(),
						damage);

				if(entityHit instanceof LivingEntity && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit)){
					((LivingEntity)entityHit).addPotionEffect(new MobEffectInstance(WizardryPotions.frost,
							Spells.iceball.getProperty(Spell.EFFECT_DURATION).intValue(),
							Spells.iceball.getProperty(Spell.EFFECT_STRENGTH).intValue()));
				}

			}else{

				BlockPos pos = rayTrace.getBlockPos();

				if(rayTrace.sideHit == Direction.UP && !world.isRemote && world.isSideSolid(pos, Direction.UP)
						&& BlockUtils.canBlockBeReplaced(world, pos.up()) && BlockUtils.canPlaceBlock(thrower, world, pos)){
					world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
				}
			}

			this.playSound(WizardrySounds.ENTITY_ICEBALL_HIT, 2, 0.8f + rand.nextFloat() * 0.3f);

			this.setDead();
		}
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){

			for(int i=0; i<5; i++){

				double dx = (rand.nextDouble() - 0.5) * width;
				double dy = (rand.nextDouble() - 0.5) * height + this.height/2;
				double dz = (rand.nextDouble() - 0.5) * width;
				double v = 0.06;
				ParticleBuilder.create(ParticleBuilder.Type.SNOW)
						.pos(this.getPositionVector().add(dx - this.motionX/2, dy, dz - this.motionZ/2))
						.vel(-v * dx, -v * dy, -v * dz).scale(width*2).time(8 + rand.nextInt(4)).spawn(world);

				if(ticksExisted > 1){
					dx = (rand.nextDouble() - 0.5) * width;
					dy = (rand.nextDouble() - 0.5) * height + this.height / 2;
					dz = (rand.nextDouble() - 0.5) * width;
					ParticleBuilder.create(ParticleBuilder.Type.SNOW)
							.pos(this.getPositionVector().add(dx - this.motionX, dy, dz - this.motionZ))
							.vel(-v * dx, -v * dy, -v * dz).scale(width*2).time(8 + rand.nextInt(4)).spawn(world);
				}
			}
		}
	}

	@Override
	public int getLifetime(){
		return 16;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
