package electroblob.wizardry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

/** Like {@link net.minecraft.world.level.block.BlockFrostedIce}, but for lava instead of water. */
// This is mostly copied from that class, with a few changes
public class BlockObsidianCrust extends Block {

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

	public BlockObsidianCrust(){
        super(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
	}

    @Override
    public void randomTick(BlockState p_222954_, ServerLevel p_222955_, BlockPos p_222956_, RandomSource p_222957_) {
        this.tick(p_222954_, p_222955_, p_222956_, p_222957_);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource p_222948_) {
        if ((p_222948_.nextInt(3) == 0 || this.countNeighbors(world, pos) < 4) && world.getMaxLocalRawBrightness(pos) > 11 - state.getValue(AGE) - state.getLightBlock(world, pos)) {
            this.slightlyMelt(world, pos, state, p_222948_, true);
        } else {
            world.scheduleTick(pos, this, Mth.nextInt(p_222948_, 20, 40));
        }
    }

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean flag){
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

	protected void slightlyMelt(Level world, BlockPos pos, BlockState state, RandomSource random, boolean meltNeighbours){

		int i = state.getValue(AGE);

		if(i < 3){

			world.setBlock(pos, state.setValue(AGE, i + 1), 2);
			world.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53586_) {
        p_53586_.add(AGE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter p_53570_, BlockPos p_53571_, BlockState p_53572_) {
        return ItemStack.EMPTY;
    }
}
