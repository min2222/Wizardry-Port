package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.properties.PropertyEnum;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;

import java.util.EnumMap;

public class BlockCrystal extends Block {

    public static final PropertyEnum<Element> ELEMENT = PropertyEnum.create("element", Element.class);

    private static final EnumMap<Element, MaterialColor> map_colours = new EnumMap<>(Element.class);

    static {
    	map_colours.put(Element.MAGIC, MaterialColor.PINK);
		map_colours.put(Element.FIRE, MaterialColor.ORANGE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.ICE, MaterialColor.LIGHT_BLUE);
		map_colours.put(Element.LIGHTNING, MaterialColor.CYAN);
		map_colours.put(Element.NECROMANCY, MaterialColor.PURPLE);
		map_colours.put(Element.EARTH, MaterialColor.GREEN);
		map_colours.put(Element.SORCERY, MaterialColor.LIME);
		map_colours.put(Element.HEALING, MaterialColor.YELLOW);
	}
	
	public BlockCrystal(Material material){
		super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.MAGIC));
        this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public int damageDropped(BlockState state){
        return (state.getValue(ELEMENT)).ordinal();
    }

	@Override
	public MaterialColor getMapColor(BlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}

	@Override
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> items){
		if(this.getCreativeTab() == tab){
        	for(Element element : Element.values()){
        		items.add(new ItemStack(this, 1, element.ordinal()));
        	}
        }
	}
	
	@Override
    public BlockState getStateFromMeta(int metadata){
        return this.defaultBlockState().withProperty(ELEMENT, Element.values()[metadata]);
    }

    @Override
    public int getMetaFromState(BlockState state){
        return (state.getValue(ELEMENT)).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ELEMENT);
    }
}
