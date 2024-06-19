package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
		return new TileEntityArcaneWorkbench();
	}

	@Override
	public boolean isNormalCube(BlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(BlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(BlockState state){
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, Direction face){
		return face == Direction.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState block, Player player, InteractionHand hand,
									Direction side, float hitX, float hitY, float hitZ){

		BlockEntity tileEntity = level.getTileEntity(pos);

		if(tileEntity == null || player.isShiftKeyDown()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.ARCANE_WORKBENCH, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState block){
		
        BlockEntity tileentity = level.getTileEntity(pos);

        if(tileentity instanceof TileEntityArcaneWorkbench){
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityArcaneWorkbench)tileentity);
        }

        super.breakBlock(world, pos, block);
	}

}
