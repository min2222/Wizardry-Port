package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityPlayerSave extends BlockEntity {

	/** The UUID of the caster. As of Wizardry 4.2, this <b>is</b> synced, and rather than storing the caster
	 * instance via a weak reference, it is fetched from the UUID each time it is needed in
	 * {@link TileEntityPlayerSave#getCaster()}. */
	private UUID casterUUID;

	public TileEntityPlayerSave(){}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound){
		super.readFromNBT(tagCompound);
		if(tagCompound.hasUniqueId("casterUUID")) casterUUID = tagCompound.getUniqueId("casterUUID");
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound){

		super.writeToNBT(tagCompound);

		if(casterUUID != null){
			tagCompound.setUniqueId("casterUUID", casterUUID);
		}

		return tagCompound;
	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	@Nullable
	public LivingEntity getCaster(){

		Entity entity = EntityUtils.getEntityByUUID(world, casterUUID);

		if(entity != null && !(entity instanceof LivingEntity)){ // Should never happen
			Wizardry.logger.warn("{} has a non-living owner!", this);
			entity = null;
		}

		return (LivingEntity)entity;
	}

	public void setCaster(@Nullable LivingEntity caster){
		this.casterUUID = caster == null ? null : caster.getUniqueID();
	}

	@Override
	public final CompoundTag getUpdateTag(){
		return this.writeToNBT(new CompoundTag());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
