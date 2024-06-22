package electroblob.wizardry.block;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlockCrystalFlowerPot extends FlowerPotBlock {

	public BlockCrystalFlowerPot(@org.jetbrains.annotations.Nullable java.util.function.Supplier<FlowerPotBlock> emptyPot, java.util.function.Supplier<? extends Block> p_53528_, BlockBehaviour.Properties properties){
		super(emptyPot, p_53528_, properties);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random){
		if(world.isClientSide && random.nextBoolean()){
			ParticleBuilder.create(Type.SPARKLE)
					.pos(pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.6 + random.nextDouble() * 0.3, pos.getZ() + 0.3 + random.nextDouble() * 0.4)
					.vel(0, 0.01, 0)
					.time(20 + random.nextInt(10))
					.clr(0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2), 0.5f + (random.nextFloat() / 2))
					.spawn(world);
		}
	}

}
