package electroblob.wizardry.inventory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ContainerBookshelf extends AbstractContainerMenu {

	private static final Set<Item> validItems = new HashSet<>();

	/** The bookshelf tile entity associated with this container. */
	public TileEntityBookshelf tileentity;

	public ContainerBookshelf(int id, Inventory inventory, TileEntityBookshelf tileentity){
        super(WizardryGuiHandler.BOOKSHELF_MENU.get(), id);
		this.tileentity = tileentity;

		for(int y = 0; y < 2; y++){
			for(int x = 0; x < BlockBookshelf.SLOT_COUNT / 2; x++){
				this.addSlot(new SlotBookshelf(inventory, x + BlockBookshelf.SLOT_COUNT / 2 * y, 35 + x * 18, 17 + y * 18));
			}
		}

		for(int x = 0; x < 9; x++){
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 124));
		}

		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 9; x++){
				this.addSlot(new Slot(inventory, 9 + x + y * 9, 8 + x * 18, 66 + y * 18));
			}
		}

	}
	
    @SuppressWarnings("resource")
    public ContainerBookshelf(int i, Inventory playerInventory, FriendlyByteBuf buf) {
        this(i, playerInventory, (TileEntityBookshelf) Minecraft.getInstance().level.getBlockEntity(buf.readBlockPos()));
    }

	/** Called from individual slots when their item is changed or removed. */
	public void onSlotChanged(){
		this.tileentity.sync();
	}

	@Override
	public boolean stillValid(Player player){
		return this.tileentity.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int clickedSlotId){

		ItemStack remainder = ItemStack.EMPTY;
		Slot slot = this.slots.get(clickedSlotId);

		if(slot != null && slot.hasItem()){

			ItemStack stack = slot.getItem(); // The stack that was there originally
			remainder = stack.copy(); // A copy of that stack

			// Bookshelf -> inventory
			if(clickedSlotId < BlockBookshelf.SLOT_COUNT){
				// Tries to move the stack into the player's inventory. If this fails...
				if(!this.moveItemStackTo(stack, BlockBookshelf.SLOT_COUNT, this.slots.size(), true)){
					return ItemStack.EMPTY; // ...nothing else happens.
				}
			}
			// Inventory -> bookshelf
			else{

				int minSlotId = 0;
				int maxSlotId = BlockBookshelf.SLOT_COUNT - 1;

				if(!isBook(stack)) return ItemStack.EMPTY;

				if(!this.moveItemStackTo(stack, minSlotId, maxSlotId + 1, false)){
					return ItemStack.EMPTY;
				}
			}

			if(stack.getCount() == 0){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(stack.getCount() == remainder.getCount()){
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack);
		}

		return remainder;
	}

	/** Returns true if the given stack counts as a book and can be placed in a bookshelf, false if not. */
	public static boolean isBook(ItemStack stack){
		return validItems.contains(stack.getItem()) || Settings.containsMetaItem(Wizardry.settings.bookItems, stack);
	}

	/**
	 * Adds the given item to the set of items that can be put in a bookshelf. This method should be called from the
	 * {@code init()} phase.
	 * @param item The item to register
	 * @see BlockBookshelf#registerBookModelTexture(Supplier, ResourceLocation)
	 */
	public static void registerBookItem(Item item){
		validItems.add(item);
	}

	/** Called from {@link Wizardry#init(FMLCommonSetupEvent)} to register the default book items. */
	public static void initDefaultBookItems(){
		registerBookItem(Items.BOOK);
		registerBookItem(Items.WRITTEN_BOOK);
		registerBookItem(Items.WRITABLE_BOOK);
		registerBookItem(Items.ENCHANTED_BOOK);
		registerBookItem(WizardryItems.SPELL_BOOK.get());
		registerBookItem(WizardryItems.ARCANE_TOME.get());
		registerBookItem(WizardryItems.WIZARD_HAND_BOOK.get());
		registerBookItem(WizardryItems.RUINED_SPELL_BOOK.get());
		registerBookItem(WizardryItems.SCROLL.get());
		registerBookItem(WizardryItems.BLANK_SCROLL.get());
		registerBookItem(WizardryItems.IDENTIFICATION_SCROLL.get());
		// Not using the map in WandHelper for consistency with BlockBookshelf (again, addons must add theirs manually)
		registerBookItem(WizardryItems.STORAGE_UPGRADE.get());
		registerBookItem(WizardryItems.SIPHON_UPGRADE.get());
		registerBookItem(WizardryItems.CONDENSER_UPGRADE.get());
		registerBookItem(WizardryItems.RANGE_UPGRADE.get());
		registerBookItem(WizardryItems.DURATION_UPGRADE.get());
		registerBookItem(WizardryItems.COOLDOWN_UPGRADE.get());
		registerBookItem(WizardryItems.BLAST_UPGRADE.get());
		registerBookItem(WizardryItems.ATTUNEMENT_UPGRADE.get());
		registerBookItem(WizardryItems.MELEE_UPGRADE.get());
	}

	public class SlotBookshelf extends Slot {

		public SlotBookshelf(Inventory inventory, int index, int x, int y){
			super(inventory, index, x, y);
		}

		@Override
		public void set(ItemStack stack){
			boolean statusChanged = this.getItem().isEmpty() != stack.isEmpty()
					|| BlockBookshelf.getBookItems().indexOf(this.getItem().getItem())
					!= BlockBookshelf.getBookItems().indexOf(stack.getItem());
			super.set(stack);
			if(statusChanged) ContainerBookshelf.this.onSlotChanged();
		}

		@Override
		public void onTake(Player player, ItemStack stack){
			ContainerBookshelf.this.onSlotChanged();
		}

		@Override
		public boolean mayPlace(ItemStack stack){
			return isBook(stack);
		}
	}

}
