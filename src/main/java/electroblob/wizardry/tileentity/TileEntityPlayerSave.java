package electroblob.wizardry.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityPlayerSave extends BlockEntity {

	/** The UUID of the caster. As of Wizardry 4.2, this <b>is</b> synced, and rather than storing the caster
	 * instance via a weak reference, it is fetched from the UUID each time it is needed in
	 * {@link TileEntityPlayerSave#getCaster()}. */
	private UUID casterUUID;

    public TileEntityPlayerSave(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.level.markAndNotifyBlock(worldPosition, this.level.getChunkAt(worldPosition), level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3, 512);
	}

	@Override
	public void load(CompoundTag tagCompound){
		super.load(tagCompound);
		if(tagCompound.hasUUID("casterUUID")) casterUUID = tagCompound.getUUID("casterUUID");
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound){

		super.saveAdditional(tagCompound);

		if(casterUUID != null){
			tagCompound.putUUID("casterUUID", casterUUID);
		}
	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	@Nullable
	public LivingEntity getCaster(){

		Entity entity = EntityUtils.getEntityByUUID(level, casterUUID);

		if(entity != null && !(entity instanceof LivingEntity)){ // Should never happen
			Wizardry.logger.warn("{} has a non-living owner!", this);
			entity = null;
		}

		return (LivingEntity)entity;
	}

	public void setCaster(@Nullable LivingEntity caster){
		this.casterUUID = caster == null ? null : caster.getUUID();
	}

}
