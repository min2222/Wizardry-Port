package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Extending BlockBush allows me to remove nearly everything from this class.
@Mod.EventBusSubscriber
public class BlockCrystalFlower extends BushBlock {

	private static final VoxelShape AABB = Shapes.create(0.5F - 0.2f, 0.0F, 0.5F - 0.2f, 0.5F + 0.2f,
			0.2f * 3.0F, 0.5F + 0.2f);

	public BlockCrystalFlower(){
		super(BlockBehaviour.Properties.of(Material.PLANT).instabreak().lightLevel((state) -> (int)7.5).randomTicks().sound(SoundType.CROP));
	}

	@Override
	public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
		return AABB;
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random){
		if(world.isClientSide && random.nextBoolean()){
			ParticleBuilder.create(Type.SPARKLE)
			.pos(pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() / 2 + 0.5, pos.getZ() + random.nextDouble()).vel(0, 0.01, 0)
					.time(20 + random.nextInt(10)).clr(0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2),
					0.5f + (random.nextFloat() / 2)).spawn(world);
		}
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos){
		return PlantType.PLAINS;
	}

	@SubscribeEvent
	public static void onBonemealEvent(BonemealEvent event){
		// Grows crystal flowers when bonemeal is used on grass
		if(Wizardry.settings.bonemealGrowsCrystalFlowers && event.getBlock().getBlock() == Blocks.GRASS){

			BlockPos pos = event.getPos().offset(event.getLevel().random.nextInt(8) - event.getLevel().random.nextInt(8),
					event.getLevel().random.nextInt(4) - event.getLevel().random.nextInt(4),
					event.getLevel().random.nextInt(8) - event.getLevel().random.nextInt(8));

			if(event.getLevel().isEmptyBlock(new BlockPos(pos))
					&& (!event.getLevel().dimension().equals(Level.NETHER) || pos.getY() < 127)
					&& WizardryBlocks.CRYSTAL_FLOWER.get().defaultBlockState().canSurvive(event.getLevel(), pos)){
				event.getLevel().setBlock(pos, WizardryBlocks.CRYSTAL_FLOWER.get().defaultBlockState(), 2);
			}
		}
	}
}
