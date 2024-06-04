package electroblob.wizardry.block;

import com.google.common.collect.Maps;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemSpectralDust;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.tileentity.TileEntityReceptacle;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BlockTorch;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class BlockReceptacle extends BlockTorch implements ITileEntityProvider {

	protected static final AABB STANDING_AABB = new AABB(4 / 16d, 0 / 16d, 4 / 16d, 12 / 16d, 8 / 16d, 12 / 16d);
	protected static final AABB NORTH_WALL_AABB = new AABB(4 / 16d, 2 / 16d, 7 / 16d, 12 / 16d, 10 / 16d, 16 / 16d);
	protected static final AABB SOUTH_WALL_AABB = new AABB(4 / 16d, 2 / 16d, 0 / 16d, 12 / 16d, 10 / 16d, 9 / 16d);
	protected static final AABB WEST_WALL_AABB = new AABB(7 / 16d, 2 / 16d, 4 / 16d, 16 / 16d, 10 / 16d, 12 / 16d);
	protected static final AABB EAST_WALL_AABB = new AABB(0 / 16d, 2 / 16d, 4 / 16d, 9 / 16d, 10 / 16d, 12 / 16d);

	private static final double WALL_PARTICLE_OFFSET = 3 / 16d;

	public static final Map<Element, int[]> PARTICLE_COLOURS;

	static{

		Map<Element, int[]> map = Maps.newEnumMap(Element.class);

		map.put(Element.MAGIC, new int[]{0xe4c7cd, 0xfeffbe, 0x9d2cf3});
		map.put(Element.FIRE, new int[]{0xff9600, 0xfffe67, 0xd02700});
		map.put(Element.ICE, new int[]{0xa3e8f4, 0xe9f9fc, 0x138397});
		map.put(Element.LIGHTNING, new int[]{0x409ee1, 0xf5f0ff, 0x225474});
		map.put(Element.NECROMANCY, new int[]{0xa811ce, 0xf575f5, 0x382366});
		map.put(Element.EARTH, new int[]{0xa8f408, 0xc8ffb2, 0x795c28});
		map.put(Element.SORCERY, new int[]{0x56e8e3, 0xe8fcfc, 0x16a64d});
		map.put(Element.HEALING, new int[]{0xfff69e, 0xfffff6, 0xa18200});

		PARTICLE_COLOURS = Maps.immutableEnumMap(map);
	}

	public BlockReceptacle(){
		super();
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(0);
		this.setLightLevel(0.5f);
		this.setSoundType(SoundType.STONE);
	}

	@Override
	public AABB getBoundingBox(BlockState state, IBlockAccess world, BlockPos pos){
		switch(state.getValue(FACING)){
			case EAST:
				return EAST_WALL_AABB;
			case WEST:
				return WEST_WALL_AABB;
			case SOUTH:
				return SOUTH_WALL_AABB;
			case NORTH:
				return NORTH_WALL_AABB;
			default:
				return STANDING_AABB;
		}
	}

	@Override
	public AABB getCollisionBoundingBox(BlockState state, IBlockAccess world, BlockPos pos){
		return state.getBoundingBox(world, pos);
	}

	@Override
	public int getLightValue(BlockState state, IBlockAccess world, BlockPos pos){

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle && ((TileEntityReceptacle)tileEntity).getElement() != null){
			return super.getLightValue(state, world, pos); // Return super to use float value from constructor
		}

		return 0;
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState state, int fortune){

		super.getDrops(drops, world, pos, state, fortune);

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle){
			Element element = ((TileEntityReceptacle)tileEntity).getElement();
			if(element != null) drops.add(new ItemStack(WizardryItems.spectral_dust, 1, element.ordinal()));
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(Level world, BlockPos pos, Direction side){

		if(side != Direction.UP && side != Direction.DOWN
				&& world.getBlockState(pos.offset(side.getOpposite())).getBlock() instanceof BlockImbuementAltar){
			return true;
		}
		return super.canPlaceBlockOnSide(world, pos, side);
	}

	@Override
	protected boolean checkForDrop(Level world, BlockPos pos, BlockState state){
		if(state.getBlock() == this && this.canPlaceAt(world, pos, state.getValue(FACING))){
			return true;
		}
		return super.checkForDrop(world, pos, state);
	}

	@Override
	protected boolean onNeighborChangeInternal(Level world, BlockPos pos, BlockState state){
		// This is so stupid, BlockTorch DUPLICATES the code that checks for valid placement in the super method instead
		// of checking the existing methods...
		return !this.checkForDrop(world, pos, state); // Literally all we need. None of the rubbish in super.
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer){

		if(this.canPlaceAt(world, pos, facing)){
			return this.getDefaultState().withProperty(FACING, facing);
		}else{

			for(Direction enumfacing : Direction.Plane.HORIZONTAL){
				if(this.canPlaceAt(world, pos, enumfacing)){
					return this.getDefaultState().withProperty(FACING, enumfacing);
				}
			}

			return this.getDefaultState();
		}
	}

	// Why is everything in BlockTorch private, for goodness sake...
	private boolean canPlaceAt(Level world, BlockPos pos, Direction facing){

		BlockPos blockpos = pos.offset(facing.getOpposite());
		BlockState state = world.getBlockState(blockpos);
		Block block = state.getBlock();
		BlockFaceShape blockfaceshape = state.getBlockFaceShape(world, blockpos, facing);

		if(facing.equals(Direction.UP) && this.canPlaceOn(world, blockpos)){
			return true;
		}else if(facing != Direction.UP && facing != Direction.DOWN){
			return !isExceptBlockForAttachWithPiston(block) && (blockfaceshape == BlockFaceShape.SOLID || block instanceof BlockImbuementAltar);
		}else{
			return false;
		}
	}

	private boolean canPlaceOn(Level world, BlockPos pos){
		BlockState state = world.getBlockState(pos);
		return state.getBlock().canPlaceTorchOnTop(state, world, pos);
	}

	// See BlockFlowerPot for these two (this class is essentially based on a flower pot)

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

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ){

		BlockEntity tileEntity = world.getTileEntity(pos);

		ItemStack stack = player.getHeldItem(hand);

		if(tileEntity instanceof TileEntityReceptacle){

			Element currentElement = ((TileEntityReceptacle)tileEntity).getElement();

			if(currentElement == null){

				if(stack.getItem() instanceof ItemSpectralDust && stack.getMetadata() >= 0
						&& stack.getMetadata() < Element.values().length){

					((TileEntityReceptacle)tileEntity).setElement(Element.values()[stack.getMetadata()]);
					if(!player.capabilities.isCreativeMode) stack.shrink(1);
					world.playSound(pos.getX(), pos.getY(), pos.getZ(), WizardrySounds.BLOCK_RECEPTACLE_IGNITE,
							SoundSource.BLOCKS, 0.7f, 0.7f, false);
					return true;
				}

			}else{

				((TileEntityReceptacle)tileEntity).setElement(null);

				ItemStack dust = new ItemStack(WizardryItems.spectral_dust, 1, currentElement.ordinal());

				if(stack.isEmpty()){
					player.setHeldItem(hand, dust);
				}else if(!player.addItemStackToInventory(dust)){
					player.dropItem(dust, false);
				}

				return true;
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public void randomDisplayTick(BlockState state, Level world, BlockPos pos, Random rand){

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle){

			Element element = ((TileEntityReceptacle)tileEntity).getElement();

			if(element != null){

				Direction facing = state.getValue(FACING).getOpposite();

				Vec3 centre = GeometryUtils.getCentre(pos);
				if(facing.getAxis().isHorizontal()){
					centre = centre.add(new Vec3(facing.getDirectionVec()).scale(WALL_PARTICLE_OFFSET)).add(0, 0.125, 0);
				}

				int[] colours = PARTICLE_COLOURS.get(element);

				ParticleBuilder.create(ParticleBuilder.Type.FLASH).pos(centre).scale(0.35f).time(48).clr(colours[0]).spawn(world);

				double r = 0.12;

				for(int i = 0; i < 3; i++){

					double x = r * (rand.nextDouble() * 2 - 1);
					double y = r * (rand.nextDouble() * 2 - 1);
					double z = r * (rand.nextDouble() * 2 - 1);

					ParticleBuilder.create(ParticleBuilder.Type.DUST).pos(centre.x + x, centre.y + y, centre.z + z)
							.vel(x * -0.03, 0.02, z * -0.03).time(24 + rand.nextInt(8)).clr(colours[1]).fade(colours[2]).spawn(world);
				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		if(placer instanceof Player){
			BlockPos centre = pos.offset(world.getBlockState(pos).getValue(FACING).getOpposite());
			if(world.getBlockState(centre).getBlock() == WizardryBlocks.imbuement_altar
					&& Arrays.stream(Direction.HORIZONTALS).allMatch(f -> world.getBlockState(centre.offset(f)).getBlock() == WizardryBlocks.receptacle)){
				WizardryAdvancementTriggers.restore_imbuement_altar.triggerFor((Player)placer);
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity createNewTileEntity(Level world, int meta){
		return new TileEntityReceptacle();
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos){

		BlockEntity tileEntity = world.getTileEntity(pos);

		if(tileEntity instanceof TileEntityReceptacle){
			Element element = ((TileEntityReceptacle)tileEntity).getElement();
			return element == null ? 0 : element.ordinal() + 1;
		}

		return super.getComparatorInputOverride(state, world, pos);
	}


}
