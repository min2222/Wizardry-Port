package electroblob.wizardry.block;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.BlockBush;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

// Extending BlockBush allows me to remove nearly everything from this class.
@Mod.EventBusSubscriber
public class BlockCrystalFlower extends BlockBush {

	private static final net.minecraft.world.phys.AABB AABB = new AABB(0.5F - 0.2f, 0.0F, 0.5F - 0.2f, 0.5F + 0.2f,
			0.2f * 3.0F, 0.5F + 0.2f);

	public BlockCrystalFlower(Material par2Material){
		super(par2Material);
		this.setLightLevel(0.5f);
		this.setTickRandomly(true);
		this.setSoundType(SoundType.PLANT);
	}

	@Override
	public net.minecraft.world.phys.AABB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos){
		return AABB;
	}

	@Override
	public void randomDisplayTick(BlockState state, Level world, BlockPos pos, Random random){
		if(world.isRemote && random.nextBoolean()){
			ParticleBuilder.create(Type.SPARKLE)
			.pos(pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() / 2 + 0.5, pos.getZ() + random.nextDouble()).vel(0, 0.01, 0)
					.time(20 + random.nextInt(10)).clr(0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2),
					0.5f + (random.nextFloat() / 2)).spawn(world);
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos){
		return EnumPlantType.Plains;
	}

	@SubscribeEvent
	public static void onBonemealEvent(BonemealEvent event){
		// Grows crystal flowers when bonemeal is used on grass
		if(Wizardry.settings.bonemealGrowsCrystalFlowers && event.getBlock().getBlock() == Blocks.GRASS){

			BlockPos pos = event.getPos().add(event.getWorld().rand.nextInt(8) - event.getWorld().rand.nextInt(8),
					event.getWorld().rand.nextInt(4) - event.getWorld().rand.nextInt(4),
					event.getWorld().rand.nextInt(8) - event.getWorld().rand.nextInt(8));

			if(event.getWorld().isAirBlock(new BlockPos(pos))
					&& (!event.getWorld().provider.isNether() || pos.getY() < 127)
					&& WizardryBlocks.crystal_flower.canPlaceBlockAt(event.getWorld(), pos)){
				event.getWorld().setBlockState(pos, WizardryBlocks.crystal_flower.getDefaultState(), 2);
			}
		}
	}
}
