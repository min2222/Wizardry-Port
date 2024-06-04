package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Transportation;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.Location;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockTransportationStone extends Block {

	private static final net.minecraft.world.phys.AABB AABB = new AABB(0.0625f * 5, 0, 0.0625f * 5, 0.0625f * 11, 0.0625f * 6,
			0.0625f * 11);

	public BlockTransportationStone(Material material){
		super(material);
		this.setTickRandomly(true);
	}

	@Override
	public net.minecraft.world.phys.AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

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

	// ... and this one is for rendering.
	@Override
	public boolean isOpaqueCube(BlockState state){
		return false;
	}

	@Override
	public boolean isSideSolid(BlockState base_state, IBlockAccess world, BlockPos pos, Direction side){
		return side == Direction.DOWN;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos){

		super.neighborChanged(state, world, pos, block, fromPos);

		if(!world.isSideSolid(pos.down(), Direction.UP, false)){
			this.dropBlockAsItem(world, pos, level.getBlockState(pos), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void updateTick(Level world, BlockPos pos, BlockState state, Random random){

		if(!world.isSideSolid(pos.down(), Direction.UP)){
			this.dropBlockAsItem(world, pos, level.getBlockState(pos), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos){
		return super.canPlaceBlockAt(world, pos) && world.isSideSolid(pos.down(), Direction.UP);
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand,
                                    Direction side, float hitX, float hitY, float hitZ){

		ItemStack stack = player.getItemInHand(hand);

		if(stack.getItem() instanceof ISpellCastingItem){
			if(WizardData.get(player) != null){

				WizardData data = WizardData.get(player);

				for(int x = -1; x <= 1; x++){
					for(int z = -1; z <= 1; z++){
						BlockPos pos1 = pos.add(x, 0, z);
						if(testForCircle(world, pos1)){

							Location here = new Location(pos1, player.dimension);

							List<Location> locations = data.getVariable(Transportation.LOCATIONS_KEY);
							if(locations == null) data.setVariable(Transportation.LOCATIONS_KEY, locations = new ArrayList<>(Transportation.MAX_REMEMBERED_LOCATIONS));

							if(ItemArtefact.isArtefactActive(player, WizardryItems.charm_transportation)){

								if(locations.contains(here)){
									locations.remove(here);
									if(!level.isClientSide) player.sendStatusMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.forget", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

								}else{

									locations.add(here);
									if(!level.isClientSide) player.sendStatusMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.remember", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

									if(locations.size() > Transportation.MAX_REMEMBERED_LOCATIONS){
										Location removed = locations.remove(0);
										if(!level.isClientSide) player.sendStatusMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.forget", removed.pos.getX(), removed.pos.getY(), removed.pos.getZ(), removed.dimension), true);
									}
								}

							}else{
								if(locations.isEmpty()) locations.add(here);
								else{
									locations.remove(here); // Prevents duplicates
									if(locations.isEmpty()) locations.add(here);
									else locations.set(Math.max(locations.size() - 1, 0), here);
								}
								if(!level.isClientSide) player.sendStatusMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.confirm", Spells.transportation.getNameForTranslationFormatted()), true);
							}

							return true;
						}
					}
				}

				if(!level.isClientSide){
					player.sendStatusMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.invalid"), true);
				}else{

					BlockPos centre = findMostLikelyCircle(world, pos);
					// Displays particles in the required shape
					for(int x = -1; x <= 1; x++){
						for(int z = -1; z <= 1; z++){
							if(x == 0 && z == 0) continue;
							ParticleBuilder.create(ParticleBuilder.Type.PATH)
									.pos(GeometryUtils.getCentre(centre).add(x, -0.3125, z)).clr(0x86ff65)
									.time(200).scale(2).spawn(world);
						}
					}
				}

				return true;
			}
		}
		return false;
	}

	/** Returns whether the specified location is surrounded by a complete circle of 8 transportation stones. */
	public static boolean testForCircle(Level world, BlockPos pos){

		if(level.getBlockState(pos).getMaterial().blocksMovement() || level.getBlockState(pos.up()).getMaterial()
				.blocksMovement()) return false;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				if(level.getBlockState(pos.add(x, 0, z)).getBlock() != WizardryBlocks.transportation_stone){
					return false;
				}
			}
		}

		return true;
	}

	private static BlockPos findMostLikelyCircle(Level world, BlockPos pos){

		int bestSoFar = 0;
		BlockPos result = null;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				BlockPos pos1 = pos.add(x, 0, z);
				int n = getCircleCompleteness(world, pos1);
				if(n > bestSoFar){
					bestSoFar = n;
					result = pos1;
				}
			}
		}

		return result;
	}

	private static int getCircleCompleteness(Level world, BlockPos pos){

		int n = 0;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				if(level.getBlockState(pos.add(x, 0, z)).getBlock() == WizardryBlocks.transportation_stone) n++;
			}
		}

		return n;
	}
}
