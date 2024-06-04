package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.properties.PropertyBool;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class BlockImbuementAltar extends Block implements ITileEntityProvider {

	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	private static final net.minecraft.world.phys.AABB AABB = new AABB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0);

	public BlockImbuementAltar(){
		super(Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(6000000);
		this.setLightLevel(0.4f);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, ACTIVE);
	}

	@Override
	public BlockState getStateFromMeta(int meta){
		return this.defaultBlockState().withProperty(ACTIVE, meta == 1);
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(ACTIVE) ? 1 : 0;
	}

	@Override
	public net.minecraft.world.phys.AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public BlockEntity createNewTileEntity(Level world, int metadata){
		return new TileEntityImbuementAltar();
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
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
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
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos){
		return state.getValue(ACTIVE) ? super.getLightValue(state, world, pos) : 0;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbour){

		boolean shouldBeActive = Arrays.stream(Direction.HORIZONTALS)
				.allMatch(s -> world.getBlockState(pos.relative(s)).getBlock() == WizardryBlocks.receptacle
							&& world.getBlockState(pos.relative(s)).getValue(BlockReceptacle.FACING) == s);

		if(world.getBlockState(pos).getValue(ACTIVE) != shouldBeActive){ // Only set when it actually needs changing

			// Get contents of altar before replacing it
			BlockEntity te = world.getTileEntity(pos);
			ItemStack stack = ItemStack.EMPTY;
			if(te instanceof TileEntityImbuementAltar) stack = ((TileEntityImbuementAltar)te).getItem();

			world.setBlockAndUpdate(pos, world.getBlockState(pos).withProperty(ACTIVE, shouldBeActive));

			// Copy over contents of altar from before to new tile entity
			te = world.getTileEntity(pos);
			if(te instanceof TileEntityImbuementAltar) ((TileEntityImbuementAltar)te).setStack(stack);

			world.checkLight(pos);
		}

		BlockEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity instanceof TileEntityImbuementAltar){
			((TileEntityImbuementAltar)tileEntity).checkRecipe();
		}
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState block, Player player, InteractionHand hand,
                                    Direction side, float hitX, float hitY, float hitZ){

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(!(tileEntity instanceof TileEntityImbuementAltar) || player.isShiftKeyDown()){
			return false;
		}

		ItemStack currentStack = ((TileEntityImbuementAltar)tileEntity).getItem();
		ItemStack toInsert = player.getItemInHand(hand);

		if(currentStack.isEmpty()){
			ItemStack stack = toInsert.copy();
			stack.setCount(1);
			((TileEntityImbuementAltar)tileEntity).setStack(stack);
			((TileEntityImbuementAltar)tileEntity).setLastUser(player);
			if(!player.isCreative()) toInsert.shrink(1);

		}else{

			if(toInsert.isEmpty()){
				player.setHeldItem(hand, currentStack);
			}else if(!player.addItemStackToInventory(currentStack)){
				player.dropItem(currentStack, false);
			}

			((TileEntityImbuementAltar)tileEntity).setStack(ItemStack.EMPTY);
			((TileEntityImbuementAltar)tileEntity).setLastUser(null);
		}

		return true;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState block){

        BlockEntity tileentity = world.getTileEntity(pos);

        if(tileentity instanceof TileEntityImbuementAltar){
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((TileEntityImbuementAltar)tileentity).getItem());
        }

        super.breakBlock(world, pos, block);
	}

}
