package electroblob.wizardry.tileentity;

import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityTimer extends BlockEntity {

	public int timer = 0;
	public int maxTimer;

    public TileEntityTimer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileEntityTimer(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxTimer) {
        this(type, pos, state);
        this.maxTimer = maxTimer;
    }

    public static void update(Level world, BlockPos pos, BlockState state, TileEntityTimer tileEntity) {
    	tileEntity.timer++;

		if(tileEntity.maxTimer > 0 && tileEntity.timer > tileEntity.maxTimer && !world.isClientSide){// && this.level.getBlockId(xCoord, yCoord, zCoord) ==
														// Wizardry.magicLight.blockID){
			if(tileEntity.getBlockState() == WizardryBlocks.VANISHING_COBWEB.get().defaultBlockState()){
				// destroyBlock breaks the block as if broken by a player, with sound and particles.
				world.destroyBlock(pos, false);
			}else{
				world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			}
		}
	}

	public void setLifetime(int lifetime){
		this.maxTimer = lifetime;
	}

	public int getLifetime(){
		return maxTimer;
	}

	@Override
	public void load(CompoundTag tagCompound){
		super.load(tagCompound);
		timer = tagCompound.getInt("timer");
		maxTimer = tagCompound.getInt("maxTimer");
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound){
		super.saveAdditional(tagCompound);
		tagCompound.putInt("timer", timer);
		tagCompound.putInt("maxTimer", maxTimer);
	}
}
