package electroblob.wizardry.tileentity;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityStatue extends BlockEntity {

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

	public TileEntityStatue(BlockPos pPos, BlockState pBlockState) {
		super(WizardryBlocks.STATUE_BLOCK_ENTITY.get(), pPos, pBlockState);
	}
	
	public TileEntityStatue(BlockPos pPos, BlockState pBlockState, boolean isIce) {
		this(pPos, pBlockState);
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
			creature.setPos(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY(), this.getBlockPos().getZ() + 0.5);
	}

	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox(){

		AABB bb = INFINITE_EXTENT_AABB;
		BlockState type = getBlockState();
		// Allows the renderer to render the entity even when the bottom block is not visible.
		// Was only done for position 1, now done for all positions so the breaking animation works properly.
		if(this.creature != null){
			// Now uses the entity's bounding box
			bb = this.creature.getBoundingBoxForCulling();
			// bb = new AxisAlignedBB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + this.parts, zCoord + 1);

		}else if(type != null){

			AABB cbb = this.getLevel().getBlockState(worldPosition).getCollisionShape(level, worldPosition).bounds();
			if(cbb != null){
				bb = cbb;
			}
		}
		return bb;
	}

	public static void update(Level world, BlockPos pos, BlockState p_155016_, TileEntityStatue p_155017_) {

		p_155017_.timer++;

		// System.out.println(entityName);
		if(p_155017_.creature == null && p_155017_.entityName != null){
			p_155017_.creature = (Mob) ForgeRegistries.ENTITY_TYPES.getValue(p_155017_.entityName).create(p_155017_.level);
			if(p_155017_.creature != null){
				p_155017_.creature.load(p_155017_.entityCompound);
				p_155017_.creature.yHeadRot = p_155017_.entityYawHead;
				p_155017_.creature.yBodyRot  = p_155017_.entityYawOffset;
			}
		}

		// System.out.println("Coords: " + p_155017_.xCoord + ", " + p_155017_.yCoord + ", " + p_155017_.zCoord);
		// Breaks the block at light levels of 7 or below, with a higher chance the lower the light level.
		// The chance is (8 - light level)/12, so at light 0 the chance is 3/4 and at light 7 the chance is 1/12.
		if(!p_155017_.level.isClientSide && p_155017_.timer % 200 == 0 && p_155017_.timer > p_155017_.lifetime && !p_155017_.isIce && p_155017_.position == 1){
			if(BlockUtils.getLightLevel(world, pos) < world.random.nextInt(12) - 3){
				// p_155017_ is all that is needed because destroyBlock invokes the breakBlock function in
				// BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				world.destroyBlock(pos, false);
			}
		}

		// Breaks the block after 30 secs
		if(!p_155017_.level.isClientSide && p_155017_.timer > p_155017_.lifetime && p_155017_.isIce){
			if(p_155017_.position == 1){
				// p_155017_ is all that is needed because destroyBlock invokes the breakBlock function in
				// BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				world.destroyBlock(pos, false);
			}
		}
	}

	@Override
	public void load(CompoundTag tagCompound){
		super.load(tagCompound);
		position = tagCompound.getInt("position");
		parts = tagCompound.getInt("parts");
		entityCompound = tagCompound.getCompound("entity");
		entityName = new ResourceLocation(tagCompound.getString("entityName"));
		timer = tagCompound.getInt("timer");
		lifetime = tagCompound.getInt("lifetime");
		isIce = tagCompound.getBoolean("isIce");
		entityYawHead = tagCompound.getFloat("entityYawHead");
		entityYawOffset = tagCompound.getFloat("entityYawOffset");
	}

	@Override
	public void saveAdditional(CompoundTag tagCompound){
		super.saveAdditional(tagCompound);
		tagCompound.putInt("position", position);
		tagCompound.putInt("parts", parts);
		entityCompound = new CompoundTag();
		if(creature != null){
			creature.save(entityCompound);
			tagCompound.putString("entityName", creature.getEncodeId());
			tagCompound.putFloat("entityYawHead", creature.yHeadRot);
			tagCompound.putFloat("entityYawOffset", creature.yBodyRot);
		}
		NBTExtras.storeTagSafely(tagCompound, "entity", entityCompound);
		tagCompound.putInt("timer", timer);
		tagCompound.putInt("lifetime", lifetime);
		tagCompound.putBoolean("isIce", isIce);
	}
}
