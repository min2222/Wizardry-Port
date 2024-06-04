package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class BlockPermafrost extends BlockDryFrostedIce {

	protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 0, 1);
	protected static final AxisAlignedBB SELECTION_BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 0.125, 1);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return BOUNDING_BOX;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, Level worldIn, BlockPos pos){
		return SELECTION_BOUNDING_BOX;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
		return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state){
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, Direction face){
		return face == Direction.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public void onEntityCollision(Level world, BlockPos pos, IBlockState state, Entity entity){

		if(EntityUtils.isLiving(entity) && entity.ticksExisted % 30 == 0){
			// Can't make it player damage unless we make this block a tile entity, but there will be too many for that
			entity.attackEntityFrom(DamageSource.MAGIC, Spells.permafrost.getProperty(Spell.DAMAGE).floatValue());
			int duration = Spells.permafrost.getProperty(Spell.EFFECT_DURATION).intValue();
			int amplifier = Spells.permafrost.getProperty(Spell.EFFECT_STRENGTH).intValue();
			((LivingEntity)entity).addPotionEffect(new PotionEffect(WizardryPotions.frost, duration, amplifier));
		}

		// EntityLivingBase's slipperiness code doesn't get the block below it properly so slipperiness only works for
		// full blocks...
		if(entity.onGround){

			// Not brilliant but it's about the best I can do
			entity.motionX *= 1.12 - entity.motionX * entity.motionX;
			entity.motionZ *= 1.12 - entity.motionZ * entity.motionZ;

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
