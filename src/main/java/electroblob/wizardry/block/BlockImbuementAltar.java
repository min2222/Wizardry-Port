package electroblob.wizardry.block;

import java.util.Arrays;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class BlockImbuementAltar extends BaseEntityBlock {

	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	private static final VoxelShape AABB = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);

	public BlockImbuementAltar(){
		super(BlockBehaviour.Properties.of(Material.STONE).strength(-1.0F, 6000000.0F).lightLevel((state) -> 6));
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> p_49915_) {
		p_49915_.add(ACTIVE);
	}

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return AABB;
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TileEntityImbuementAltar(p_153215_, p_153216_);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_49232_) {
		return RenderShape.MODEL;
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getValue(ACTIVE) ? super.getLightEmission(state, level, pos) : 0;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbour, boolean flag){

		Direction[] HORIZONTALS = ObfuscationReflectionHelper.getPrivateValue(Direction.class, null, "f_122349_");
		boolean shouldBeActive = Arrays.stream(HORIZONTALS)
				.allMatch(s -> world.getBlockState(pos.relative(s)).getBlock() == WizardryBlocks.RECEPTACLE.get()
							&& world.getBlockState(pos.relative(s)).getValue(BlockReceptacle.FACING) == s);

		if(world.getBlockState(pos).getValue(ACTIVE) != shouldBeActive){ // Only set when it actually needs changing

			// Get contents of altar before replacing it
			BlockEntity te = world.getBlockEntity(pos);
			ItemStack stack = ItemStack.EMPTY;
			if(te instanceof TileEntityImbuementAltar) stack = ((TileEntityImbuementAltar)te).getStack();

			world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ACTIVE, shouldBeActive));

			// Copy over contents of altar from before to new tile entity
			te = world.getBlockEntity(pos);
			if(te instanceof TileEntityImbuementAltar) ((TileEntityImbuementAltar)te).setStack(stack);

			world.getChunkSource().getLightEngine().checkBlock(pos);
		}

		BlockEntity tileEntity = world.getBlockEntity(pos);
		if(tileEntity instanceof TileEntityImbuementAltar){
			((TileEntityImbuementAltar)tileEntity).checkRecipe();
		}
	}

	@Override
	public InteractionResult use(BlockState p_60503_, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_60508_) {

		BlockEntity tileEntity = world.getBlockEntity(pos);

		if(!(tileEntity instanceof TileEntityImbuementAltar) || player.isShiftKeyDown()){
			return InteractionResult.FAIL;
		}

		ItemStack currentStack = ((TileEntityImbuementAltar)tileEntity).getStack();
		ItemStack toInsert = player.getItemInHand(hand);

		if(currentStack.isEmpty()){
			ItemStack stack = toInsert.copy();
			stack.setCount(1);
			((TileEntityImbuementAltar)tileEntity).setStack(stack);
			((TileEntityImbuementAltar)tileEntity).setLastUser(player);
			if(!player.isCreative()) toInsert.shrink(1);

		}else{

			if(toInsert.isEmpty()){
				player.setItemInHand(hand, currentStack);
			}else if(!player.addItem(currentStack)){
				player.drop(currentStack, false);
			}

			((TileEntityImbuementAltar)tileEntity).setStack(ItemStack.EMPTY);
			((TileEntityImbuementAltar)tileEntity).setLastUser(null);
		}

		return InteractionResult.SUCCESS;
	}
	
	@Override
	public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
		
        BlockEntity tileentity = p_60516_.getBlockEntity(p_60517_);

        if(tileentity instanceof TileEntityImbuementAltar){
            Containers.dropItemStack(p_60516_, p_60517_.getX(), p_60517_.getY(), p_60517_.getZ(), ((TileEntityImbuementAltar)tileentity).getStack());
        }
        
		super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
	}

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTicker(p_153273_, p_153275_, WizardryBlocks.IMBUEMENT_ALTAR_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level p_151988_, BlockEntityType<T> p_151989_, BlockEntityType<TileEntityImbuementAltar> p_151990_) {
        return createTickerHelper(p_151989_, p_151990_, TileEntityImbuementAltar::update);
    }
}
