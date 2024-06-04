package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import java.util.Random;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(Material material){
		super(material);
		this.setSoundType(SoundType.STONE);
		setResistance(5.0F);
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
