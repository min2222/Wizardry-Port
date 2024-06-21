package electroblob.wizardry.block;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class BlockArcaneWorkbench extends BaseEntityBlock {

	private static final VoxelShape AABB = Shapes.create(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

	public BlockArcaneWorkbench(){
        super(BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().destroyTime(1.0f).lightLevel((p_152688_) -> 12));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext ctx){
		return AABB;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new TileEntityArcaneWorkbench(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if (p_60504_.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = p_60504_.getBlockEntity(p_60505_);

            if (blockentity == null || p_60506_.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (blockentity instanceof TileEntityArcaneWorkbench) {
                NetworkHooks.openScreen((ServerPlayer) p_60506_, (TileEntityArcaneWorkbench) blockentity, p_60505_);
            }
            return InteractionResult.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        BlockEntity blockentity = p_60516_.getBlockEntity(p_60517_);
        if (blockentity instanceof TileEntityArcaneWorkbench) {
            Containers.dropContents(p_60516_, p_60517_, (TileEntityArcaneWorkbench) blockentity);
        }
        super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153273_, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTicker(p_153273_, p_153275_, WizardryBlocks.ARCANE_WORKBENCH_BLOCK_ENTITY.get());
    }

    @Nullable
    protected static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level p_151988_, BlockEntityType<T> p_151989_, BlockEntityType<TileEntityArcaneWorkbench> p_151990_) {
        return createTickerHelper(p_151989_, p_151990_, TileEntityArcaneWorkbench::update);
    }

}
