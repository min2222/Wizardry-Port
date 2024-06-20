package electroblob.wizardry.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class BlockGildedWood extends Block {

	public BlockGildedWood(){
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD));
	}

}
