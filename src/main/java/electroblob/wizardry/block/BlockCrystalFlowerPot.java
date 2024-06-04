package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockCrystalFlowerPot extends Block {

	protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public BlockCrystalFlowerPot(){
		super(Material.CIRCUITS);
		this.setLightLevel(0.4f);
		this.setTickRandomly(true);
	}

	@Override
	public void randomDisplayTick(IBlockState state, Level world, BlockPos pos, Random random){
		if(world.isRemote && random.nextBoolean()){
			ParticleBuilder.create(Type.SPARKLE)
					.pos(pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.6 + random.nextDouble() * 0.3, pos.getZ() + 0.3 + random.nextDouble() * 0.4)
					.vel(0, 0.01, 0)
					.time(20 + random.nextInt(10))
					.clr(0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2))
					.spawn(world);
		}
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(Level world, IBlockState state){
		return new TileEntityFlowerPot(Item.getItemFromBlock(WizardryBlocks.crystal_flower), 0);
	}

	@Override
	public boolean hasTileEntity(IBlockState state){
		return true;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, IBlockState state, Player player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ){

		ItemStack stack = new ItemStack(WizardryBlocks.crystal_flower);

		if(player.getHeldItem(hand).isEmpty()){
			player.setHeldItem(hand, stack);
		}else if(!player.addItemStackToInventory(stack)){
			player.dropItem(stack, false);
		}

		world.setBlockState(pos, Blocks.FLOWER_POT.getDefaultState());

		return true;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, Level world, BlockPos pos, Player player){
		return new ItemStack(WizardryBlocks.crystal_flower);
	}

	@Override
	public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
		super.getDrops(drops, world, pos, state, fortune);
		drops.add(new ItemStack(WizardryBlocks.crystal_flower));
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return Items.FLOWER_POT;
	}

	// The rest are identical behaviour to BlockFlowerPot

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return BOUNDING_BOX;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos){
		IBlockState downState = world.getBlockState(pos.down());
		return super.canPlaceBlockAt(world, pos) && (downState.isTopSolid() || downState.getBlockFaceShape(world, pos.down(), Direction.UP) == BlockFaceShape.SOLID);
	}

	@Override
	public void neighborChanged(IBlockState state, Level world, BlockPos pos, Block block, BlockPos neighbour){
		IBlockState downState = world.getBlockState(pos.down());
		if(!downState.isTopSolid() && downState.getBlockFaceShape(world, pos.down(), Direction.UP) != BlockFaceShape.SOLID){
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, Direction face){
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, Level world, BlockPos pos, Player player, boolean willHarvest){
		if(willHarvest) return true; // If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(Level world, Player player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool){
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}

}
