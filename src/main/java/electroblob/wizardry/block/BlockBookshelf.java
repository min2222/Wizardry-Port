package electroblob.wizardry.block;

import com.google.common.collect.ImmutableList;
import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.world.level.block.BlockHorizontal;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.ITileEntityProvider;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.properties.PropertyInteger;
import net.minecraft.world.level.block.state.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockStateContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class BlockBookshelf extends HorizontalDirectionalBlock implements EntityBlock {

	/** When a bookshelf block (of any kind specified in the config) is added or removed, players within this range will
	 * be notified of the change. */
	public static final double PLAYER_NOTIFY_RANGE = 32;
	public static final int SLOT_COUNT = 12;

	public static final UnlistedPropertyInt[] BOOKS = new UnlistedPropertyInt[SLOT_COUNT];

	private static final Map<Supplier<Item>, ResourceLocation> BOOK_TEXTURE_MAP = new HashMap<>();
	private static ImmutableList<Item> bookItems;
	private static ImmutableList<ResourceLocation> bookTextures;

	public BlockBookshelf(){
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setSoundType(SoundType.WOOD);
	}

	@Override
	protected BlockStateContainer createBlockState(){
//		IProperty<?>[] properties = { FACING };
//		return new BlockStateContainer(this, ArrayUtils.addAll(properties, BOOKS));
		return new BlockStateContainer.Builder(this).add(FACING).add(BOOKS).build();
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockState state, BlockPos pos, EnumFacing face){
		return state.getValue(FACING).getAxis() == face.getAxis() ? BlockFaceShape.UNDEFINED : BlockFaceShape.SOLID;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer){
		return this.defaultBlockState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	// Why on earth are these two not in BlockHorizontal?

	@Override
	public BlockState withRotation(BlockState state, Rotation rotation){
		return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState withMirror(BlockState state, Mirror mirror){
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	public void onBlockAdded(Level worldIn, BlockPos pos, BlockState state){
		super.onBlockAdded(worldIn, pos, state);
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState block){

		BlockEntity tileentity = level.getTileEntity(pos);

		if(tileentity instanceof TileEntityBookshelf){
			InventoryHelper.dropInventoryItems(world, pos, (TileEntityBookshelf)tileentity);
		}

		super.breakBlock(world, pos, block); // For blocks that don't extend BlockContainer, this removes the TE
	}

	@Override
	public BlockState getStateFromMeta(int meta){
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
		if(enumfacing.getAxis() == EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH;
		return this.defaultBlockState().withProperty(FACING, enumfacing);
	}

	@Override
	public int getMetaFromState(BlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos){

		IExtendedBlockState s = (IExtendedBlockState)super.getExtendedState(state, world, pos);

		if(level.getTileEntity(pos) instanceof TileEntityBookshelf){

			TileEntityBookshelf tileentity = ((TileEntityBookshelf)level.getTileEntity(pos));

			for(int i = 0; i < tileentity.getSizeInventory(); i++){

				if(tileentity.getStackInSlot(i).isEmpty()){
					s = s.withProperty(BOOKS[i], bookItems.size());

				}else{
					Item item = tileentity.getStackInSlot(i).getItem();
					// Default to the standard spell book texture, which will always be the first item in the list
					s = s.withProperty(BOOKS[i], bookItems.contains(item) ? bookItems.indexOf(item) : 0);
				}
			}
		}

		return s;
	}

	@Nullable
	@Override
	public BlockEntity createNewTileEntity(Level world, int meta){
		return new TileEntityBookshelf();
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState block, Player player, EnumHand hand,
                                    EnumFacing side, float hitX, float hitY, float hitZ){

		BlockEntity tileEntity = level.getTileEntity(pos);

		if(tileEntity == null || player.isShiftKeyDown()){
			return false;
		}

		player.openGui(Wizardry.instance, WizardryGuiHandler.BOOKSHELF, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, Level world, BlockPos pos){

		BlockEntity tileEntity = level.getTileEntity(pos);

		if(tileEntity instanceof TileEntityBookshelf){

			int slotsOccupied = 0;

			for(int i = 0; i < ((TileEntityBookshelf)tileEntity).getSizeInventory(); i++){
				if(!((TileEntityBookshelf)tileEntity).getStackInSlot(i).isEmpty()) slotsOccupied++;
			}

			return slotsOccupied;

		}

		return super.getComparatorInputOverride(state, world, pos);
	}

	@Override
	public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param){
		super.eventReceived(state, world, pos, id, param);
		BlockEntity tileentity = level.getTileEntity(pos);
		return tileentity != null && tileentity.receiveClientEvent(id, param);
	}

	/** Returns the list of book textures, in ID order. */
	public static ImmutableList<ResourceLocation> getBookTextures(){
		// ModelBookshelf calls this before init() so we need to return the map values as a fallback
		return bookTextures == null ? ImmutableList.copyOf(BOOK_TEXTURE_MAP.values()) : bookTextures;
	}

	/** Returns the list of registered book items, in ID order. */
	public static ImmutableList<Item> getBookItems(){
		return bookItems;
	}

	/** Called during block registration to initialise the block properties for each book. */
	public static void initBookProperties(){
		for(int i=0; i<SLOT_COUNT; i++){
			BOOKS[i] = new UnlistedPropertyInt("book" + i);
		}
	}

	// Based on BlockFluid's UnlistedPropertyBool; this is only needed because of Java's weird restrictions on generics
	private static final class UnlistedPropertyInt extends Properties.PropertyAdapter<Integer> {

		public UnlistedPropertyInt(String name){
			// Max is inclusive here, but we're using the largest value for no book so there's an extra one (can't use -1)
			super(PropertyInteger.create(name, 0, BOOK_TEXTURE_MAP.size()));
		}
	}

	/** Called from {@link Wizardry#init(FMLInitializationEvent)} to retrieve the actual book items and compile them
	 * and their textures into immutable lists. */
	public static void compileBookModelTextures(){

		ImmutableList.Builder<Item> itemListBuider = ImmutableList.builder();
		ImmutableList.Builder<ResourceLocation> textureListBuilder = ImmutableList.builder();

		for(Map.Entry<Supplier<Item>, ResourceLocation> entry : BOOK_TEXTURE_MAP.entrySet()){
			itemListBuider.add(entry.getKey().get());
			textureListBuilder.add(entry.getValue());
		}

		bookItems = itemListBuider.build();
		bookTextures = textureListBuilder.build();
	}

	/** Called from {@link Wizardry#preInit(FMLPreInitializationEvent)} to register the default set of book textures. */
	public static void registerStandardBookModelTextures(){
		// Vanilla Minecraft books
		// Regular brown books are the default so they're registered first
		registerBookModelTexture(() -> Items.BOOK, 							new ResourceLocation(Wizardry.MODID, "blocks/books_brown"));
		registerBookModelTexture(() -> Items.WRITABLE_BOOK, 				new ResourceLocation(Wizardry.MODID, "blocks/books_brown"));
		registerBookModelTexture(() -> Items.WRITTEN_BOOK, 					new ResourceLocation(Wizardry.MODID, "blocks/books_brown"));
		registerBookModelTexture(() -> Items.ENCHANTED_BOOK, 				new ResourceLocation(Wizardry.MODID, "blocks/books_enchanted"));
		// Wizardry books
		registerBookModelTexture(() -> WizardryItems.spell_book, 			new ResourceLocation(Wizardry.MODID, "blocks/books_red"));
		registerBookModelTexture(() -> WizardryItems.wizard_handbook, 		new ResourceLocation(Wizardry.MODID, "blocks/books_blue"));
		registerBookModelTexture(() -> WizardryItems.arcane_tome, 			new ResourceLocation(Wizardry.MODID, "blocks/books_purple"));
		registerBookModelTexture(() -> WizardryItems.ruined_spell_book, 	new ResourceLocation(Wizardry.MODID, "blocks/books_brown"));
		registerBookModelTexture(() -> WizardryItems.scroll, 				new ResourceLocation(Wizardry.MODID, "blocks/scrolls_blue"));
		registerBookModelTexture(() -> WizardryItems.blank_scroll, 			new ResourceLocation(Wizardry.MODID, "blocks/scrolls_blue"));
		registerBookModelTexture(() -> WizardryItems.identification_scroll, new ResourceLocation(Wizardry.MODID, "blocks/scrolls_purple"));
		// Can't use the map in WandHelper because this is called from preInit (addons will have to add theirs manually)
		registerBookModelTexture(() -> WizardryItems.storage_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.siphon_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.condenser_upgrade, 	new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.range_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.duration_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.cooldown_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.blast_upgrade, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.ATTUNEMENT_UPGRADE, 	new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
		registerBookModelTexture(() -> WizardryItems.MELEE_UPGRADE, 		new ResourceLocation(Wizardry.MODID, "blocks/scrolls_wooden"));
	}

	/**
	 * Registers a book texture to be used for the book model when the given item is placed in a bookshelf. This method
	 * <b>must</b> be called from {@code preInit} as the registered item count is required during block registration.
	 * This also necessitates the use of a supplier to fetch the item, since the items will not yet be registered.
	 * @param itemFactory A {@link Supplier} that returns the book item to link the texture to. This item need not
	 *                    be an instance of {@link electroblob.wizardry.item.ItemSpellBook ItemSpellBook}. Duplicate
	 *                    items will result in an {@link IllegalArgumentException} being thrown later.
	 * @param texture The texture to apply to the book model.
	 */
	public static void registerBookModelTexture(Supplier<Item> itemFactory, ResourceLocation texture){
		BOOK_TEXTURE_MAP.put(itemFactory, texture);
	}

	/**
	 * Returns a list of nearby bookshelves' inventories, where 'bookshelves' are any tile entities with inventories
	 * whose blocks are specified in the config file under the {@code bookshelfBlocks} option.
	 * @param world The world to search in
	 * @param centre The position to search around
	 * @param exclude Any tile entities that should be excluded from the returned list
	 * @return A list of nearby {@link IInventory} objects that count as valid bookshelves
	 */
	public static List<Container> findNearbyBookshelves(Level world, BlockPos centre, BlockEntity... exclude){

		List<Container> bookshelves = new ArrayList<>();

		int searchRadius = Wizardry.settings.bookshelfSearchRadius;

		for(int x = -searchRadius; x <= searchRadius; x++){
			for(int y = -searchRadius; y <= searchRadius; y++){
				for(int z = -searchRadius; z <= searchRadius; z++){

					BlockPos pos = centre.offset(x, y, z);

					if(Settings.containsMetaBlock(Wizardry.settings.bookshelfBlocks, world.getBlockState(pos))){
						BlockEntity te = world.getBlockEntity(pos);
						if(te instanceof Container && !ArrayUtils.contains(exclude, te)) bookshelves.add((Container)te);
					}

				}
			}
		}

		return bookshelves;

	}

	@SubscribeEvent
	public static void onWorldLoadEvent(WorldEvent.Load event){
		event.getWorld().addEventListener(Listener.instance);
	}

	@SubscribeEvent
	public static void onWorldUnloadEvent(WorldEvent.Unload event){
		event.getWorld().removeEventListener(Listener.instance);
	}

	public static class Listener implements IWorldEventListener {

		public static final Listener instance = new Listener();

		private Listener(){}

		@Override
		public void notifyBlockUpdate(Level world, BlockPos pos, BlockState oldState, BlockState newState, int flags){

			if(oldState == newState) return; // Probably won't happen but just in case

			if(Settings.containsMetaBlock(Wizardry.settings.bookshelfBlocks, oldState) // Bookshelf removed
					|| Settings.containsMetaBlock(Wizardry.settings.bookshelfBlocks, newState)){ // Bookshelf placed
				// It is also possible (with commands) for a bookshelf to be replaced with another bookshelf, in which
				// case this should still just be called once
				Wizardry.proxy.notifyBookshelfChange(world, pos);
			}

		}

		// Dummy implementations
		@Override public void notifyLightSet(BlockPos pos){}
		@Override public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2){}
		@Override public void playSoundToAllNearExcept(@Nullable Player player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch){}
		@Override public void playRecord(SoundEvent soundIn, BlockPos pos){}
		@Override public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters){}
		@Override public void spawnParticle(int id, boolean ignoreRange, boolean minimiseParticleLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters){}
		@Override public void onEntityAdded(Entity entityIn){}
		@Override public void onEntityRemoved(Entity entityIn){}
		@Override public void broadcastSound(int soundID, BlockPos pos, int data){}
		@Override public void playEvent(Player player, int type, BlockPos blockPosIn, int data){}
		@Override public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress){}

	}

}
