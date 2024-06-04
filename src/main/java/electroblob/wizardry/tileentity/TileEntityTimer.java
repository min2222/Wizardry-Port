package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockVanishingCobweb;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ITickable;

public class TileEntityTimer extends BlockEntity implements ITickable {

	public int timer = 0;
	public int maxTimer;

	public TileEntityTimer(){

	}

	public TileEntityTimer(int maxTimer){
		this.maxTimer = maxTimer;
	}

	@Override
	public void update(){

		timer++;

		if(maxTimer > 0 && timer > maxTimer && !this.world.isRemote){// && this.world.getBlockId(xCoord, yCoord, zCoord) ==
														// Wizardry.magicLight.blockID){
			if(this.getBlockType() instanceof BlockVanishingCobweb){
				// destroyBlock breaks the block as if broken by a player, with sound and particles.
				this.world.destroyBlock(pos, false);
			}else{
				this.world.setBlockToAir(pos);
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
	public void readFromNBT(CompoundTag tagCompound){
		super.readFromNBT(tagCompound);
		timer = tagCompound.getInteger("timer");
		maxTimer = tagCompound.getInteger("maxTimer");
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("timer", timer);
		tagCompound.setInteger("maxTimer", maxTimer);
		return tagCompound;
	}

	@Override
	public final CompoundTag getUpdateTag(){
		return this.writeToNBT(new CompoundTag());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		CompoundTag tag = new CompoundTag();
		writeToNBT(tag);
		return new SPacketUpdateTileEntity(pos, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		CompoundTag tag = pkt.getNbtCompound();
		readFromNBT(tag);
	}

}
