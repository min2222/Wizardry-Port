package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(){
		super(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(3, 5).requiresCorrectToolForDrops());
		setHarvestLevel("pickaxe", 2);
	}

	@Override
	public int quantityDropped(BlockState state, int fortune, Random random){
		// This now works the same way as vanilla ores
		if(fortune > 0){
			
			int i = random.nextInt(fortune + 2) - 1;
			
            if(i < 0) i = 0;
            
			return (random.nextInt(3) + 1) * (i + 1);
			
		}else{
			return random.nextInt(3) + 1;
		}
	}

	@Override
	public Item getItemDropped(BlockState state, Random random, int fortune){
		return WizardryItems.magic_crystal;
	}
	
	@Override
	public int getExpDrop(BlockState state, IBlockAccess world, BlockPos pos, int fortune){
		
		Random rand = world instanceof Level ? ((Level)world).rand : RANDOM;
		
        if(this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)){
            return Mth.getInt(rand, 1, 4);
        }
        
        return 0;
	}
}
