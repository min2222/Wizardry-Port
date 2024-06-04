package electroblob.wizardry.block;

import net.minecraft.block.BlockFrostedIce;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Random;

/** Like {@link BlockFrostedIce}, but melting does not depend on light level or neighbouring blocks, and it just
 * disappears instead of turning to water. */
public class BlockDryFrostedIce extends BlockFrostedIce {

	public BlockDryFrostedIce(){
		super();
		setDefaultSlipperiness(0.98f); // For some strange reason BlockFrostedIce overrides BlockIce's icy-ness...
		setSoundType(SoundType.GLASS);
	}

	@Override
	public Material getMaterial(BlockState state){
		return Material.ICE; // For goodness sake
	}

	@Override
	protected void turnIntoWater(Level world, BlockPos pos){
		world.destroyBlock(pos, false);
	}

	@Override
	public void updateTick(Level worldIn, BlockPos pos, BlockState state, Random rand){
		if(rand.nextInt(3) == 0){
			this.slightlyMelt(worldIn, pos, state, rand, true);
		}else{
			worldIn.scheduleUpdate(pos, this, Mth.getInt(rand, 20, 40));
		}
	}
}
