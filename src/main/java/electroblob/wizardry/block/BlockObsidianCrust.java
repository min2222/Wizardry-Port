package electroblob.wizardry.block;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockObsidian;
import net.minecraft.world.level.block.properties.PropertyInteger;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

/** Like {@link net.minecraft.world.level.block.BlockFrostedIce}, but for lava instead of water. */
// This is mostly copied from that class, with a few changes
public class BlockObsidianCrust extends BlockObsidian {

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

	public BlockObsidianCrust(){
		this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(AGE);
	}

	@Override
	public BlockState getStateFromMeta(int meta){
		return this.defaultBlockState().withProperty(AGE, Mth.clamp(meta, 0, 3));
	}

	@Override
	public void updateTick(Level world, BlockPos pos, BlockState state, Random random){
		if((random.nextInt(3) == 0 || this.countNeighbors(world, pos) < 4) && world.getLightFromNeighbors(pos) > 11 - state.getValue(AGE) - state.getLightOpacity()){
			this.slightlyMelt(world, pos, state, random, true);
		}else{
			world.scheduleUpdate(pos, this, Mth.getInt(random, 20, 40));
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos){
		if(block == this){
			int i = this.countNeighbors(world, pos);

			if(i < 2){
				this.melt(world, pos);
			}
		}
	}

	private int countNeighbors(Level world, BlockPos pos){

		int i = 0;

		for(Direction enumfacing : Direction.values()){
			if(world.getBlockState(pos.relative(enumfacing)).getBlock() == this){
				++i;

				if(i >= 4){
					return i;
				}
			}
		}

		return i;
	}

	protected void slightlyMelt(Level world, BlockPos pos, BlockState state, Random random, boolean meltNeighbours){

		int i = state.getValue(AGE);

		if(i < 3){

			world.setBlockAndUpdate(pos, state.withProperty(AGE, i + 1), 2);
			world.scheduleUpdate(pos, this, Mth.getInt(random, 20, 40));

		}else{

			this.melt(world, pos);

			if(meltNeighbours){

				for(Direction enumfacing : Direction.values()){

					BlockPos blockpos = pos.relative(enumfacing);
					BlockState iblockstate = world.getBlockState(blockpos);

					if(iblockstate.getBlock() == this){
						this.slightlyMelt(world, blockpos, iblockstate, random, false);
					}
				}
			}
		}
	}

	protected void melt(Level world, BlockPos pos){
		world.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
		world.neighborChanged(pos, Blocks.LAVA, pos);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, AGE);
	}

	@Override
	public ItemStack getItem(Level world, BlockPos pos, BlockState state){
		return ItemStack.EMPTY;
	}
}
