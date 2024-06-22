package electroblob.wizardry.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(){
		super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3, 5).requiresCorrectToolForDrops());
		setHarvestLevel("pickaxe", 2); //BlockTags.NEEDS_IRON_TOOL;
	}

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        return Mth.nextInt(randomSource, 1, 4);
    }
}
