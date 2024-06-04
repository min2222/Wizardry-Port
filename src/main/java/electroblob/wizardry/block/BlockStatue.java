package electroblob.wizardry.block;

import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockStatue extends Block implements ITileEntityProvider {

	private boolean isIce;

	/** The NBT tag name for storing the petrified flag (used for rendering) in the target's tag compound. */
	public static final String PETRIFIED_NBT_KEY = "petrified";
	/** The NBT tag name for storing the frozen flag (used for rendering) in the target's tag compound. */
	public static final String FROZEN_NBT_KEY = "frozen";

	public BlockStatue(Material material){
		super(material);
		this.isIce = material == Material.ICE;
		if(this.isIce){
			this.setDefaultSlipperiness(0.98f);
			this.setSoundType(SoundType.GLASS);
		}
	}

	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos){
		// Not a good idea to call getBlockBoundsMinX() or whatever from in here, since this method changes those!
		if(!this.isIce){

			if(world.getTileEntity(pos) instanceof TileEntityStatue){

				TileEntityStatue statue = (TileEntityStatue)world.getTileEntity(pos);

				if(statue.creature != null){

					// Block bounds are set to match the width and height of the entity, clamped to within 1 block.
					return new AABB((float)Math.max(0.5 - statue.creature.width / 2, 0), 0,
							(float)Math.max(0.5 - statue.creature.width / 2, 0),
							(float)Math.min(0.5 + statue.creature.width / 2, 1),
							// This checks if the block is the top one and if so reduces its height so the top lines up
							// with
							// the top of the entity model.
							statue.position == statue.parts
									? (float)Math.min(statue.creature.getBbHeight() - statue.parts + 1, 1)
									: 1,
							(float)Math.min(0.5 + statue.creature.width / 2, 1));
				}
			}
		}

		return FULL_BLOCK_AABB;
	}

	// getCollisionBoundingBox eventually calls getBoundingBox anyway, and since I want the collision box and the block
	// outline to be the same here, I've removed that getCollisionBoundingBox entirely.

	// The number of these methods is quite simply ridiculous. This one seems to be for placement logic and block
	// connections (fences, glass panes, etc.)...
	@Override
	public boolean isFullCube(BlockState state){
		return false;
	}

	// ...this one isn't used much but has something to do with redstone...
	@Override
	public boolean isBlockNormalCube(BlockState state){
		return false;
	}

	// ... this one is for most other game logic...
	@Override
	public boolean isNormalCube(BlockState state){
		return false;
	}

	// Forge version of the above method. I still need to override both though because vanilla uses the other one.
	@Override
	public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	// ... and this one is for rendering.
	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return this.isIce ? BlockRenderLayer.TRANSLUCENT : BlockRenderLayer.SOLID;
	}

	@Override
	public EnumBlockRenderType getRenderType(BlockState state){
		return this.isIce ? EnumBlockRenderType.MODEL : EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityStatue(this.isIce);
	}

	@Override
	public int quantityDropped(Random random){
		return 0;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state){

		if(!level.isClientSide){

			TileEntityStatue tileentity = (TileEntityStatue)world.getTileEntity(pos);

			if(tileentity != null){
				if(tileentity.parts == 2){
					if(tileentity.position == 2){
						world.destroyBlock(pos.down(), false);
					}else{
						world.destroyBlock(pos.up(), false);
					}
				}else if(tileentity.parts == 3){
					if(tileentity.position == 3){
						world.destroyBlock(pos.down(), false);
						world.destroyBlock(pos.down(2), false);
					}else if(tileentity.position == 2){
						world.destroyBlock(pos.down(), false);
						world.destroyBlock(pos.up(), false);
					}else{
						world.destroyBlock(pos.up(), false);
						world.destroyBlock(pos.up(2), false);
					}
				}
			}

			// This is only when position == 1 because world.destroyBlock calls this function for the other blocks.
			if(tileentity != null && tileentity.position == 1 && tileentity.creature != null){
				tileentity.creature.getEntityData().removeTag(BlockStatue.PETRIFIED_NBT_KEY);
				tileentity.creature.isDead = false;
				world.spawnEntity(tileentity.creature);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@SuppressWarnings("deprecation")
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos,
                                        Direction side){

		BlockState iblockstate = blockAccess.getBlockState(pos.relative(side));
		Block block = iblockstate.getBlock();

		return this.isIce && block == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}
	
	/**
	 * Turns the given entity into a statue. The type of statue depends on the block instance this method was invoked on.
	 * @param target The entity to turn into a statue.
	 * @param caster The entity that caused it, or null if it was not caused by an entity
	 * @param duration The time for which the entity should remain a statue. For petrified creatures, this is the minimum
	 * time it can stay as a statue.
	 * @return True if the entity was successfully turned into a statue, false if not (i.e. something was in the way).
	 */
	// Making this an instance method means it works equally well for both types of statue
	public boolean convertToStatue(Mob target, @Nullable LivingEntity caster, int duration){
		
		if(target.deathTime > 0) return false;

		BlockPos pos = new BlockPos(target);
		Level world = target.world;

		target.hurtTime = 0; // Stops the entity looking red while frozen and the resulting z-fighting
		target.extinguish();

		// Short mobs such as spiders and pigs
		if((target.getBbHeight() < 1.2 || target.isChild()) && BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canPlaceBlock(caster, world, pos)){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getTileEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 1);
				((TileEntityStatue)world.getTileEntity(pos)).setLifetime(duration);
			}
			
			target.getEntityData().setBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
		// Normal sized mobs like zombies and skeletons
		else if(target.getBbHeight() < 2.5 && BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canBlockBeReplaced(world, pos.up())
				&& BlockUtils.canPlaceBlock(caster, world, pos) && BlockUtils.canPlaceBlock(caster, world, pos.up())){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getTileEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 2);
				((TileEntityStatue)world.getTileEntity(pos)).setLifetime(duration);
			}

			world.setBlockAndUpdate(pos.up(), this.defaultBlockState());
			if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 2);
			}

			target.getEntityData().setBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
		// Tall mobs like endermen
		else if(BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canBlockBeReplaced(world, pos.up()) && BlockUtils.canBlockBeReplaced(world, pos.up(2))
				&& BlockUtils.canPlaceBlock(caster, world, pos) && BlockUtils.canPlaceBlock(caster, world, pos.up()) && BlockUtils.canPlaceBlock(caster, world, pos.up(2))){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getTileEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos)).setCreatureAndPart(target, 1, 3);
				((TileEntityStatue)world.getTileEntity(pos)).setLifetime(duration);
			}

			world.setBlockAndUpdate(pos.up(), this.defaultBlockState());
			if(world.getTileEntity(pos.up()) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos.up())).setCreatureAndPart(target, 2, 3);
			}

			world.setBlockAndUpdate(pos.up(2), this.defaultBlockState());
			if(world.getTileEntity(pos.up(2)) instanceof TileEntityStatue){
				((TileEntityStatue)world.getTileEntity(pos.up(2))).setCreatureAndPart(target, 3, 3);
			}

			target.getEntityData().setBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
			
		return false;
	}
	
}
