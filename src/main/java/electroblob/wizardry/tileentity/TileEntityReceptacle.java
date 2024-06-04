package electroblob.wizardry.tileentity;

import electroblob.wizardry.constants.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class TileEntityReceptacle extends BlockEntity {

	private Element element;

	public TileEntityReceptacle(){
		this(null);
	}

	public TileEntityReceptacle(Element element){
		this.element = element;
	}

	@Nullable
	public Element getElement(){
		return element;
	}

	public void setElement(@Nullable Element element){
		this.element = element;
		world.notifyNeighborsRespectDebug(pos, blockType, true); // Update altar if connected
		world.checkLight(pos);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound){
		super.writeToNBT(compound);
		compound.setInteger("Element", element == null ? -1 : element.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound){
		super.readFromNBT(compound);
		int i = compound.getInteger("Element");
		element = i == -1 ? null : Element.values()[i];
	}

	@Override
	public CompoundTag getUpdateTag(){
		return this.writeToNBT(new CompoundTag());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
