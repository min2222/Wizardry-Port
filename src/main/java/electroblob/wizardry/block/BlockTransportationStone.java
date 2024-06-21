package electroblob.wizardry.block;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockTransportationStone extends Block {

	private static final VoxelShape AABB = Shapes.create(0.0625f * 5, 0, 0.0625f * 5, 0.0625f * 11, 0.0625f * 6,
			0.0625f * 11);

	public BlockTransportationStone(BlockBehaviour.Properties properties){
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter source, BlockPos pos, CollisionContext ctx){
		return AABB;
	}

	@Override
	public BlockState updateShape(BlockState p_51032_, Direction p_51033_, BlockState p_51034_, LevelAccessor p_51035_, BlockPos p_51036_, BlockPos p_51037_) {
		return !p_51032_.canSurvive(p_51035_, p_51036_) ? Blocks.AIR.defaultBlockState() : p_51032_;
	}
	
	@Override
	public boolean canSurvive(BlockState p_60525_, LevelReader p_60526_, BlockPos p_60527_) {
		return p_60525_.isFaceSturdy(p_60526_, p_60527_.below(), Direction.UP);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult blockHit){

		ItemStack stack = player.getItemInHand(hand);

		if(stack.getItem() instanceof ISpellCastingItem){
			if(WizardData.get(player) != null){

				WizardData data = WizardData.get(player);

				for(int x = -1; x <= 1; x++){
					for(int z = -1; z <= 1; z++){
						BlockPos pos1 = pos.offset(x, 0, z);
						if(testForCircle(world, pos1)){

							Location here = new Location(pos1, player.level.dimension().location().getPath());

							List<Location> locations = data.getVariable(Transportation.LOCATIONS_KEY);
							if(locations == null) data.setVariable(Transportation.LOCATIONS_KEY, locations = new ArrayList<>(Transportation.MAX_REMEMBERED_LOCATIONS));

							if(ItemArtefact.isArtefactActive(player, WizardryItems.CHARM_TRANSPORTATION.get())){

								if(locations.contains(here)){
									locations.remove(here);
									if(!world.isClientSide) player.displayClientMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.forget", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

								}else{

									locations.add(here);
									if(!world.isClientSide) player.displayClientMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.remember", here.pos.getX(), here.pos.getY(), here.pos.getZ(), here.dimension), true);

									if(locations.size() > Transportation.MAX_REMEMBERED_LOCATIONS){
										Location removed = locations.remove(0);
										if(!world.isClientSide) player.displayClientMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.forget", removed.pos.getX(), removed.pos.getY(), removed.pos.getZ(), removed.dimension), true);
									}
								}

							}else{
								if(locations.isEmpty()) locations.add(here);
								else{
									locations.remove(here); // Prevents duplicates
									if(locations.isEmpty()) locations.add(here);
									else locations.set(Math.max(locations.size() - 1, 0), here);
								}
								if(!world.isClientSide) player.displayClientMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.confirm", Spells.TRANSPORTATION.getNameForTranslationFormatted()), true);
							}

							return InteractionResult.SUCCESS;
						}
					}
				}

				if(!world.isClientSide){
					player.displayClientMessage(Component.translatable("tile." + Wizardry.MODID + ":transportation_stone.invalid"), true);
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

				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	/** Returns whether the specified location is surrounded by a complete circle of 8 transportation stones. */
	public static boolean testForCircle(Level world, BlockPos pos){

		if(world.getBlockState(pos).getMaterial().blocksMotion() || world.getBlockState(pos.above()).getMaterial()
				.blocksMotion()) return false;

		for(int x = -1; x <= 1; x++){
			for(int z = -1; z <= 1; z++){
				if(x == 0 && z == 0) continue;
				if(world.getBlockState(pos.offset(x, 0, z)).getBlock() != WizardryBlocks.TRANSPORTATION_STONE.get()){
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
				BlockPos pos1 = pos.offset(x, 0, z);
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
				if(world.getBlockState(pos.offset(x, 0, z)).getBlock() == WizardryBlocks.TRANSPORTATION_STONE.get()) n++;
			}
		}

		return n;
	}
}
