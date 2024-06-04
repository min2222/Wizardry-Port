package electroblob.wizardry.tileentity;

import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityList;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityStatue extends BlockEntity implements ITickable {

	public Mob creature;
	private CompoundTag entityCompound;
	private ResourceLocation entityName;
	private float entityYawHead;
	private float entityYawOffset;
	public boolean isIce;
	private int timer;
	private int lifetime = 600;
	/**
	 * <b>[Client-side]</b> Keeps track of the destroy stage for the block associated with this tile entity for
	 * rendering purposes. Will be 0 if position is not 1.
	 */
	public int destroyStage;

	public TileEntityStatue(){

	}

	public TileEntityStatue(boolean isIce){
		this.isIce = isIce;
		this.timer = 0;
	}

	/** The number of stone blocks that this petrified creature is made of. */
	public int parts;

	/**
	 * The position within the petrified creature this particular tileentity holds. 1 is at the bottom.
	 */
	// TODO: Remove this, there is no need for more than 1 TE per statue - actually, there is in the case of petrified
	// 		 creatures, which should show the block breaking animation on all parts... except this is also broken!
	public int position = 1;

	public void setCreatureAndPart(Mob entity, int position, int parts){
		this.creature = entity;
		this.position = position;
		this.parts = parts;
		// Aligns the entity with the block so the render bounding box works correctly, and also for visual effect when
		// broken out.
		if(position == 1)
			creature.setPosition(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ() + 0.5);
	}

	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox(){

		AABB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		// Allows the renderer to render the entity even when the bottom block is not visible.
		// Was only done for position 1, now done for all positions so the breaking animation works properly.
		if(this.creature != null){
			// Now uses the entity's bounding box
			bb = this.creature.getRenderBoundingBox();
			// bb = new AxisAlignedBB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + this.parts, zCoord + 1);

		}else if(type != null){

			AABB cbb = this.getWorld().getBlockState(pos).getBoundingBox(world, pos);
			if(cbb != null){
				bb = cbb;
			}
		}
		return bb;
	}

	@Override
	public boolean canRenderBreaking(){
		return true;
	}

	@Override
	public void update(){

		this.timer++;

		// System.out.println(entityName);
		if(this.creature == null && entityName != null){
			this.creature = (Mob)EntityList.createEntityByIDFromName(this.entityName, this.world);
			if(this.creature != null){
				this.creature.readFromNBT(entityCompound);
				this.creature.rotationYawHead = this.entityYawHead;
				this.creature.renderYawOffset = this.entityYawOffset;
			}
		}

		// System.out.println("Coords: " + this.xCoord + ", " + this.yCoord + ", " + this.zCoord);
		// Breaks the block at light levels of 7 or below, with a higher chance the lower the light level.
		// The chance is (8 - light level)/12, so at light 0 the chance is 3/4 and at light 7 the chance is 1/12.
		if(!this.world.isRemote && this.timer % 200 == 0 && this.timer > lifetime && !this.isIce && this.position == 1){
			if(BlockUtils.getLightLevel(world, pos) < this.world.rand.nextInt(12) - 3){
				// This is all that is needed because destroyBlock invokes the breakBlock function in
				// BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.world.destroyBlock(pos, false);
			}
		}

		// Breaks the block after 30 secs
		if(!this.world.isRemote && this.timer > lifetime && this.isIce){
			if(this.position == 1){
				// This is all that is needed because destroyBlock invokes the breakBlock function in
				// BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.world.destroyBlock(pos, false);
			}
		}
	}

	@Override
	public void readFromNBT(CompoundTag tagCompound){
		super.readFromNBT(tagCompound);
		position = tagCompound.getInt("position");
		parts = tagCompound.getInt("parts");
		entityCompound = tagCompound.getCompoundTag("entity");
		entityName = new ResourceLocation(tagCompound.getString("entityName"));
		timer = tagCompound.getInt("timer");
		lifetime = tagCompound.getInt("lifetime");
		isIce = tagCompound.getBoolean("isIce");
		entityYawHead = tagCompound.getFloat("entityYawHead");
		entityYawOffset = tagCompound.getFloat("entityYawOffset");
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.putInt("position", position);
		tagCompound.putInt("parts", parts);
		entityCompound = new CompoundTag();
		if(creature != null){
			creature.writeToNBT(entityCompound);
			tagCompound.setString("entityName", EntityList.getKey(creature).toString());
			tagCompound.setFloat("entityYawHead", creature.rotationYawHead);
			tagCompound.setFloat("entityYawOffset", creature.renderYawOffset);
		}
		NBTExtras.storeTagSafely(tagCompound, "entity", entityCompound);
		tagCompound.putInt("timer", timer);
		tagCompound.putInt("lifetime", lifetime);
		tagCompound.setBoolean("isIce", isIce);

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
