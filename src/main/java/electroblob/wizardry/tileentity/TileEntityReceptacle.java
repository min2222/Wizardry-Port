package electroblob.wizardry.tileentity;

import javax.annotation.Nullable;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityReceptacle extends BlockEntity {

	private Element element;

	public TileEntityReceptacle(BlockPos p_155229_, BlockState p_155230_) {
		super(WizardryBlocks.RECEPTACLE_BLOCK_ENTITY.get(), p_155229_, p_155230_);
	}

	public TileEntityReceptacle(Element element, BlockPos p_155229_, BlockState p_155230_){
		super(WizardryBlocks.RECEPTACLE_BLOCK_ENTITY.get(), p_155229_, p_155230_);
		this.element = element;
	}

	@Nullable
	public Element getElement(){
		return element;
	}

	public void setElement(@Nullable Element element){
		this.element = element;
		level.blockUpdated(worldPosition, getBlockState().getBlock()); // Update altar if connected
		level.getChunkSource().getLightEngine().checkBlock(worldPosition);
	}

	@Override
	public void saveAdditional(CompoundTag compound){
		super.saveAdditional(compound);
		compound.putInt("Element", element == null ? -1 : element.ordinal());
	}

	@Override
	public void load(CompoundTag compound){
		super.load(compound);
		int i = compound.getInt("Element");
		element = i == -1 ? null : Element.values()[i];
	}

}
