package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

public class BlockArcaneWorkbench extends BlockContainer {

	private static final net.minecraft.world.phys.AABB AABB = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

	public BlockArcaneWorkbench(){
		super(Material.ROCK);
		this.setLightLevel(0.8f);
	}

	@Override
	public net.minecraft.world.phys.AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public TileEntity createNewTileEntity(Level world, int metadata){
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

		TileEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity == null || player.isSneaking()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.ARCANE_WORKBENCH, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState block){
		
        TileEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityArcaneWorkbench){
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityArcaneWorkbench)tileentity);
        }

        super.breakBlock(world, pos, block);
	}

}
