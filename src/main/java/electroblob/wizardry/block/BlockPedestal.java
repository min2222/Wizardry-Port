package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityShrineCore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.properties.PropertyBool;
import net.minecraft.world.level.block.properties.PropertyEnum;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;

public class BlockPedestal extends Block implements ITileEntityProvider {

	public static final IntegerProperty ELEMENT = IntegerProperty.create("element", 0, Element.values().length); // Everything except MAGIC

	// A 'natural' pedestal is one that was generated as part of a structure, is unbreakable and has a tileentity
	public static final PropertyBool NATURAL = PropertyBool.create("natural");

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

	public BlockPedestal(Material material){
		super(material);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.FIRE).withProperty(NATURAL, false));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
	}

	@Override
	public int damageDropped(BlockState state){
		return state.getValue(ELEMENT).ordinal(); // Ignore the NATURAL state here, it's unobtainable
	}

	@Override
	public MaterialColor getMapColor(BlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}

	@Override
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> items){
		// Ignore the NATURAL state here, it's unobtainable
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
	public float getBlockHardness(BlockState state, Level world, BlockPos pos){
		return state.getValue(NATURAL) ? -1 : super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(Level world, BlockPos pos, @Nullable Entity exploder, Explosion explosion){
		return level.getBlockState(pos).getValue(NATURAL) ? 6000000.0F : super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public boolean hasTileEntity(BlockState state){
		return state.getValue(NATURAL); // Only naturally-generated pedestals have a (shrine core) tile entity
	}

	@Nullable
	@Override
	public BlockEntity createNewTileEntity(Level world, int meta){
		return new TileEntityShrineCore();
	}

	@Override
	public BlockState getStateFromMeta(int metadata){
		boolean natural = false;
		if(metadata > ELEMENT.getAllowedValues().size()){
			natural = true;
			metadata -= ELEMENT.getAllowedValues().size();
		}
		Element element = Element.values()[metadata];
		if(!ELEMENT.getAllowedValues().contains(element)) return this.defaultBlockState().withProperty(NATURAL, natural);
		return this.defaultBlockState().withProperty(ELEMENT, element).withProperty(NATURAL, natural);
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(ELEMENT).ordinal() + (state.getValue(NATURAL) ? ELEMENT.getAllowedValues().size() : 0);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, ELEMENT, NATURAL);
	}

}
