package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.properties.PropertyEnum;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.EnumMap;

public class BlockRunestone extends Block {

    public static final PropertyEnum<Element> ELEMENT = PropertyEnum.create("element", Element.class,
			Arrays.copyOfRange(Element.values(), 1, Element.values().length)); // Everything except MAGIC

	private static final EnumMap<Element, MaterialColor> map_colours = new EnumMap<>(Element.class);

	static {
		map_colours.put(Element.FIRE, MaterialColor.RED_STAINED_HARDENED_CLAY);
		map_colours.put(Element.ICE, MaterialColor.LIGHT_BLUE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.LIGHTNING, MaterialColor.CYAN_STAINED_HARDENED_CLAY);
		map_colours.put(Element.NECROMANCY, MaterialColor.PURPLE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.EARTH, MaterialColor.BROWN_STAINED_HARDENED_CLAY);
		map_colours.put(Element.SORCERY, MaterialColor.GRAY);
		map_colours.put(Element.HEALING, MaterialColor.YELLOW_STAINED_HARDENED_CLAY);
	}
	
	public BlockRunestone(Material material){
		super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.FIRE));
        this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
	}
	
	@Override
	public int damageDropped(BlockState state){
        return state.getValue(ELEMENT).ordinal();
    }

	@Override
	public MaterialColor getMapColor(BlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}
	
	@Override
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> items){
		if(this.getCreativeTab() == tab){
        	for(Element element : Arrays.copyOfRange(Element.values(), 1, Element.values().length)){
        		items.add(new ItemStack(this, 1, element.ordinal()));
        	}
        }
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
	}

	@Override
    public BlockState getStateFromMeta(int metadata){
		Element element = Element.values()[metadata];
		if(!ELEMENT.getAllowedValues().contains(element)) return this.defaultBlockState();
        return this.defaultBlockState().withProperty(ELEMENT, element);
    }

    @Override
    public int getMetaFromState(BlockState state){
        return state.getValue(ELEMENT).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ELEMENT);
    }

}
