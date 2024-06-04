package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockCrystalFlowerPot extends Block {

	protected static final AABB BOUNDING_BOX = new AABB(0.3125D, 0.0D, 0.3125D, 0.6875D, 0.375D, 0.6875D);

	public BlockCrystalFlowerPot(){
		super(Material.CIRCUITS);
		this.setLightLevel(0.4f);
		this.setTickRandomly(true);
	}

	@Override
	public void randomDisplayTick(BlockState state, Level world, BlockPos pos, Random random){
		if(level.isClientSide && random.nextBoolean()){
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
	public BlockEntity createTileEntity(Level world, BlockState state){
		return new TileEntityFlowerPot(Item.getItemFromBlock(WizardryBlocks.crystal_flower), 0);
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return true;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ){

		ItemStack stack = new ItemStack(WizardryBlocks.crystal_flower);

		if(player.getItemInHand(hand).isEmpty()){
			player.setHeldItem(hand, stack);
		}else if(!player.addItemStackToInventory(stack)){
			player.dropItem(stack, false);
		}

		world.setBlockAndUpdate(pos, Blocks.FLOWER_POT.defaultBlockState());

		return true;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player){
		return new ItemStack(WizardryBlocks.crystal_flower);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune){
		super.getDrops(drops, world, pos, state, fortune);
		drops.add(new ItemStack(WizardryBlocks.crystal_flower));
	}

	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune){
		return Items.FLOWER_POT;
	}

	// The rest are identical behaviour to BlockFlowerPot

	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return BOUNDING_BOX;
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
	public boolean canPlaceBlockAt(Level world, BlockPos pos){
		BlockState downState = world.getBlockState(pos.down());
		return super.canPlaceBlockAt(world, pos) && (downState.isTopSolid() || downState.getBlockFaceShape(world, pos.down(), Direction.UP) == BlockFaceShape.SOLID);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos neighbour){
		BlockState downState = world.getBlockState(pos.down());
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockState state, BlockPos pos, Direction face){
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest){
		if(willHarvest) return true; // If it will harvest, delay deletion of the block until after getDrops
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack tool){
		super.harvestBlock(world, player, pos, state, te, tool);
		world.setBlockToAir(pos);
	}

}
