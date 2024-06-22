package electroblob.wizardry.block;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockStatue extends BaseEntityBlock {

	private boolean isIce;

	/** The NBT tag name for storing the petrified flag (used for rendering) in the target's tag compound. */
	public static final String PETRIFIED_NBT_KEY = "petrified";
	/** The NBT tag name for storing the frozen flag (used for rendering) in the target's tag compound. */
	public static final String FROZEN_NBT_KEY = "frozen";

	public BlockStatue(BlockBehaviour.Properties material, boolean isIce){
		super(material);
		this.isIce = isIce;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx){
		// Not a good idea to call getBlockBoundsMinX() or whatever from in here, since this method changes those!
		if(!this.isIce){

			if(world.getBlockEntity(pos) instanceof TileEntityStatue){

				TileEntityStatue statue = (TileEntityStatue)world.getBlockEntity(pos);

				if(statue.creature != null){

					// Block bounds are set to match the width and height of the entity, clamped to within 1 block.
					return Shapes.create((float)Math.max(0.5 - statue.creature.getBbWidth() / 2, 0), 0,
							(float)Math.max(0.5 - statue.creature.getBbWidth() / 2, 0),
							(float)Math.min(0.5 + statue.creature.getBbWidth() / 2, 1),
							// This checks if the block is the top one and if so reduces its height so the top lines up
							// with
							// the top of the entity model.
							statue.position == statue.parts
									? (float)Math.min(statue.creature.getBbHeight() - statue.parts + 1, 1)
									: 1,
							(float)Math.min(0.5 + statue.creature.getBbWidth() / 2, 1));
				}
			}
		}

		return super.getShape(state, world, pos, ctx);
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return this.isIce ? RenderShape.MODEL : RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new TileEntityStatue(pPos, pState, this.isIce);
	}
	
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTicker(p_153273_, p_153275_, WizardryBlocks.STATUE_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level p_151988_, BlockEntityType<T> p_151989_, BlockEntityType<TileEntityStatue> p_151990_) {
        return createTickerHelper(p_151989_, p_151990_, TileEntityStatue::update);
    }

	@Override
	public void destroy(LevelAccessor world, BlockPos pos, BlockState pState) {

		if(!world.isClientSide()){

			TileEntityStatue tileentity = (TileEntityStatue)world.getBlockEntity(pos);

			if(tileentity != null){
				if(tileentity.parts == 2){
					if(tileentity.position == 2){
						world.destroyBlock(pos.below(), false);
					}else{
						world.destroyBlock(pos.above(), false);
					}
				}else if(tileentity.parts == 3){
					if(tileentity.position == 3){
						world.destroyBlock(pos.below(), false);
						world.destroyBlock(pos.below(2), false);
					}else if(tileentity.position == 2){
						world.destroyBlock(pos.below(), false);
						world.destroyBlock(pos.above(), false);
					}else{
						world.destroyBlock(pos.above(), false);
						world.destroyBlock(pos.above(2), false);
					}
				}
			}

			// This is only when position == 1 because world.destroyBlock calls this function for the other blocks.
			if(tileentity != null && tileentity.position == 1 && tileentity.creature != null){
				tileentity.creature.getPersistentData().remove(BlockStatue.PETRIFIED_NBT_KEY);
				tileentity.creature.revive();
				world.addFreshEntity(tileentity.creature);
			}
		}

		super.destroy(world, pos, pState);
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

		BlockPos pos = target.blockPosition();
		Level world = target.level;

		target.hurtTime = 0; // Stops the entity looking red while frozen and the resulting z-fighting
		target.clearFire();

		// Short mobs such as spiders and pigs
		if((target.getBbHeight() < 1.2 || target.isBaby()) && BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canPlaceBlock(caster, world, pos)){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getBlockEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos)).setCreatureAndPart(target, 1, 1);
				((TileEntityStatue)world.getBlockEntity(pos)).setLifetime(duration);
			}
			
			target.getPersistentData().putBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
		// Normal sized mobs like zombies and skeletons
		else if(target.getBbHeight() < 2.5 && BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canBlockBeReplaced(world, pos.above())
				&& BlockUtils.canPlaceBlock(caster, world, pos) && BlockUtils.canPlaceBlock(caster, world, pos.above())){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getBlockEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos)).setCreatureAndPart(target, 1, 2);
				((TileEntityStatue)world.getBlockEntity(pos)).setLifetime(duration);
			}

			world.setBlockAndUpdate(pos.above(), this.defaultBlockState());
			if(world.getBlockEntity(pos.above()) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos.above())).setCreatureAndPart(target, 2, 2);
			}

			target.getPersistentData().putBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
		// Tall mobs like endermen
		else if(BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canBlockBeReplaced(world, pos.above()) && BlockUtils.canBlockBeReplaced(world, pos.above(2))
				&& BlockUtils.canPlaceBlock(caster, world, pos) && BlockUtils.canPlaceBlock(caster, world, pos.above()) && BlockUtils.canPlaceBlock(caster, world, pos.above(2))){
			
			world.setBlockAndUpdate(pos, this.defaultBlockState());
			if(world.getBlockEntity(pos) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos)).setCreatureAndPart(target, 1, 3);
				((TileEntityStatue)world.getBlockEntity(pos)).setLifetime(duration);
			}

			world.setBlockAndUpdate(pos.above(), this.defaultBlockState());
			if(world.getBlockEntity(pos.above()) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos.above())).setCreatureAndPart(target, 2, 3);
			}

			world.setBlockAndUpdate(pos.above(2), this.defaultBlockState());
			if(world.getBlockEntity(pos.above(2)) instanceof TileEntityStatue){
				((TileEntityStatue)world.getBlockEntity(pos.above(2))).setCreatureAndPart(target, 3, 3);
			}

			target.getPersistentData().putBoolean(this.isIce ? FROZEN_NBT_KEY : PETRIFIED_NBT_KEY, true);
			target.discard();
			return true;
		}
			
		return false;
	}
	
}
