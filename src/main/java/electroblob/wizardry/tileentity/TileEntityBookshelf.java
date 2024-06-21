package electroblob.wizardry.tileentity;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityBookshelf extends RandomizableContainerBlockEntity {

	/** NBT key for the boolean flag specifying if this bookshelf was generated naturally as part of a structure or not.
	 * This flag is set via {@link TileEntityBookshelf#markAsNatural(CompoundTag)}. */
	private static final String NATURAL_NBT_KEY = "NaturallyGenerated";
	/** When a non-spectating player comes within this distance of a naturally-generated bookshelf, it will automatically
	 * generate its loot if a loot table was set. This means the bookshelves do not incorrectly appear empty before a
	 * player looks inside. */ // Kind of a trade-off between not seeing them appear and not triggering from miles away
	private static final int LOOT_GEN_DISTANCE = 32; // Nobody is likely to be looking from more than 32 blocks away

	/** The inventory of the bookshelf. */
	private NonNullList<ItemStack> inventory;

	/** Whether this bookshelf was generated naturally as part of a structure. This determines whether the loot is
	 * automatically generated (and synced) when a non-spectating player comes within {@value LOOT_GEN_DISTANCE} blocks,
	 * so the bookshelf doesn't look empty until opened (loot is generated with that player in the context). */
	private boolean natural;
	private boolean doNotSync;

	public TileEntityBookshelf(BlockPos pos, BlockState state){
        super(WizardryBlocks.BOOKSHELF_BLOCK_ENTITY.get(), pos, state);
		inventory = NonNullList.withSize(BlockBookshelf.SLOT_COUNT, ItemStack.EMPTY);
		// Prevent sync() happening when loading from NBT or weirdness ensues when loading a world
		// Normally I'd pass this as a flag to setInventorySlotContents but we can't change the method signature
		this.doNotSync = true;
	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		if(!this.doNotSync)
            this.level.markAndNotifyBlock(worldPosition, level.getChunkAt(worldPosition), level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3, 512);
	}

    public static void tick(Level world, BlockPos pos, BlockState state, TileEntityBookshelf tileEntity) {

    	tileEntity.doNotSync = false;

		// When a player gets near, generate the books so they can actually see them (if it was generated naturally)
		if(tileEntity.lootTable != null && tileEntity.natural){
			Player player = world.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					LOOT_GEN_DISTANCE, false);
			if(player != null){
				tileEntity.natural = false; // It's a normal bookshelf now (unlikely to matter but you never know)
				tileEntity.unpackLootTable(player);
				tileEntity.sync();
			}
		}
	}

	@Override
	public int getContainerSize(){
		return inventory.size();
	}

	// Still better to override these three because then we can sync only when necessary

	@Override
	public ItemStack removeItem(int slot, int amount){

		this.unpackLootTable(null);

		ItemStack stack = getItem(slot);

		if(!stack.isEmpty()){
			if(stack.getCount() <= amount){
				setItem(slot, ItemStack.EMPTY);
			}else{
				stack = stack.split(amount);
				if(stack.getCount() == 0){
					setItem(slot, ItemStack.EMPTY);
				}
			}
			this.setChanged();
		}

		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot){

		this.unpackLootTable(null);

		ItemStack stack = getItem(slot);

		if(!stack.isEmpty()){
			setItem(slot, ItemStack.EMPTY);
		}

		return stack;
	}

	@Override
	public void setItem(int slot, ItemStack stack){
		this.unpackLootTable(null);
		boolean wasEmpty = inventory.get(slot).isEmpty();
		super.setItem(slot, stack);
		// This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
		if(wasEmpty != stack.isEmpty()) this.sync();
		this.setChanged();
	}

	/** Sets the {@value NATURAL_NBT_KEY} flag to true in the given NBT tag compound, <b>if</b> the compound belongs to
	 * a bookshelf tile entity (more specifically, if it has an "id" tag matching the bookshelf TE's registry name). */
	public static void markAsNatural(CompoundTag nbt){
		if(nbt != null && nbt.getString("id").equals(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(WizardryBlocks.BOOKSHELF_BLOCK_ENTITY.get()).toString())){
			nbt.putBoolean(NATURAL_NBT_KEY, true);
		}
	}

	@Override
	public void unpackLootTable(@Nullable Player player){
		if(level != null && level.getServer().getLootTables() != null) super.unpackLootTable(player); // IntelliJ is wrong, it can be null
	}

    @Override
    public Component getDefaultName() {
        return Component.translatable("container." + Wizardry.MODID + ".bookshelf");
    }

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public Component getDisplayName(){
		return this.getName();
	}

	@Override
	public int getMaxStackSize(){
		return 64;
	}

	@Override
	public boolean stillValid(Player player){
		return level.getBlockEntity(worldPosition) == this && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) < 64;
	}

	@Override
	public void startOpen(Player player){

	}

	@Override
	public void stopOpen(Player player){

	}

	@Override
	public boolean canPlaceItem(int slotNumber, ItemStack stack){
		return stack.isEmpty() || ContainerBookshelf.isBook(stack);
	}

	@Override
	public void load(CompoundTag nbt){

		super.load(nbt);

		natural = nbt.getBoolean(NATURAL_NBT_KEY);

		if(!this.tryLoadLootTable(nbt)){

			ListTag tagList = nbt.getList("Inventory", Tag.TAG_COMPOUND);

			for(int i = 0; i < tagList.size(); i++){
				CompoundTag tag = tagList.getCompound(i);
				byte slot = tag.getByte("Slot");
				if(slot >= 0 && slot < getContainerSize()){
					setItem(slot, ItemStack.of(tag));
				}
			}
		}

		if(nbt.contains("CustomName", Tag.TAG_STRING)) this.setCustomName(Component.literal(nbt.getString("CustomName")));
	}

	@Override
	public void saveAdditional(CompoundTag nbt){

		super.saveAdditional(nbt);

		// Need to save this in case the block was generated but no players came near enough to trigger loot gen
		nbt.putBoolean(NATURAL_NBT_KEY, natural);

		if(!this.trySaveLootTable(nbt)){

			ListTag itemList = new ListTag();

			for(int i = 0; i < getContainerSize(); i++){
				ItemStack stack = getItem(i);
				CompoundTag tag = new CompoundTag();
				tag.putByte("Slot", (byte)i);
				stack.save(tag);
				itemList.add(tag);
			}

			NBTExtras.storeTagSafely(nbt, "Inventory", itemList);
		}

		if(this.hasCustomName()) nbt.putString("CustomName", this.getCustomName().getString());
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
		super.onDataPacket(net, pkt);
		Wizardry.proxy.notifyBookshelfChange(level, worldPosition);
	}

	@Override
	public void clearContent(){
		for(int i = 0; i < getContainerSize(); i++){
			setItem(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty(){
		for(int i = 0; i < getContainerSize(); i++){
			if(!getItem(i).isEmpty()){
				return false;
			}
		}
		return true;
	}

	@Override
	protected NonNullList<ItemStack> getItems(){
		return inventory;
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
		return new ContainerBookshelf(id, playerInventory, this);
	}

	@Override
	protected void setItems(NonNullList<ItemStack> p_59625_) {
		
	}
}
