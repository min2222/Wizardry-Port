package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPermafrost extends BlockDryFrostedIce {

    protected static final VoxelShape SELECTION_BOUNDING_BOX = Shapes.create(0, 0, 0, 1, 0.125, 1);

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SELECTION_BOUNDING_BOX;
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {

		if(EntityUtils.isLiving(entity) && entity.tickCount % 30 == 0){
			// Can't make it player damage unless we make this block a tile entity, but there will be too many for that
			entity.hurt(DamageSource.MAGIC, Spells.PERMAFROST.getProperty(Spell.DAMAGE).floatValue());
			int duration = Spells.PERMAFROST.getProperty(Spell.EFFECT_DURATION).intValue();
			int amplifier = Spells.PERMAFROST.getProperty(Spell.EFFECT_STRENGTH).intValue();
			((LivingEntity)entity).addEffect(new MobEffectInstance(WizardryPotions.FROST.get(), duration, amplifier));
		}

		// EntityLivingBase's slipperiness code doesn't get the block below it properly so slipperiness only works for
		// full blocks...
		if(entity.isOnGround()){

			// Not brilliant but it's about the best I can do
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.12 - entity.getDeltaMovement().x * entity.getDeltaMovement().x, 1, 1.12 - entity.getDeltaMovement().z * entity.getDeltaMovement().z));

//			if(entity instanceof EntityLivingBase){
//				double maxVel = 0.8;
//				double x = entity.motionX;
//				double y = entity.motionY;
//				double z = entity.motionZ;
//				double vel = MathHelper.sqrt(x*x + y*y + z*z);
//				double m = vel / maxVel;
//				if(m > 1){
//					entity.motionX /= m;
//					entity.motionZ /= m;
//				}
//			}
		}

	}
}
