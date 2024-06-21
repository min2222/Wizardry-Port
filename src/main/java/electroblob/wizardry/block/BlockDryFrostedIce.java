package electroblob.wizardry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

/** Like {@link FrostedIceBlock}, but melting does not depend on light level or neighbouring blocks, and it just
 * disappears instead of turning to water. */
public class BlockDryFrostedIce extends FrostedIceBlock {

	public BlockDryFrostedIce(){
        super(BlockBehaviour.Properties.of(Material.ICE).randomTicks().friction(0.98f).sound(SoundType.GLASS)); // For some strange reason BlockFrostedIce overrides BlockIce's icy-ness...
	}

    @Override
    protected void melt(BlockState p_54169_, Level world, BlockPos pos) {
		world.destroyBlock(pos, false);
	}

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
		if(random.nextInt(3) == 0){
			this.slightlyMelt(worldIn, pos, state, random, true);
		}else{
			worldIn.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
		}
	}
}
