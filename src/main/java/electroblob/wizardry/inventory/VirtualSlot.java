package electroblob.wizardry.inventory;

import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A {@code VirtualSlot} represents a slot in an inventory other than the one that is currently open. Like regular slots,
 * they hold a single {@link ItemStack}, but unlike regular slots, they are not drawn to the screen
 * and cannot be interacted with directly. A virtual slot is effectively a reference to a specific slot in some other
 * inventory that allows the details of that inventory to be abstracted away from the current container.
 * <p></p>
 * <i>N.B. For normal slots, {@code slotIndex == slotNumber} (index is for the inventory, number is for the container).
 * Importantly, for virtual slots this is <b>not the case</b>, since the {@code Container} they belong to is not the
 * one associated with the virtual slot's {@code IInventory}.</i>
 */
public class VirtualSlot extends Slot {

	private final BlockEntity tileEntity;
	/** Allows the virtual slot to remember what was last stored in it, so items can be put back in the same place. */
	private ItemStack prevStack; // For now this doesn't persist over GUI close

	public VirtualSlot(Container inventory, int index){
		super(inventory, index, -999, -999);
		if(!(inventory instanceof BlockEntity)) throw new IllegalArgumentException("Inventory must be a tile entity!");
		this.tileEntity = (BlockEntity)inventory;
		this.prevStack = getItem().copy(); // We MUST copy the stack or it will get changed from elsewhere later!
	}

	@Override
	public boolean isActive(){
		return false; // Virtual slots are never displayed
	}

	// We don't really want to be updating the bookshelves every single tick (let alone every frame!), so we're going
	// to have to do some 'assuming things stay the same until told otherwise'. This means it's possible that virtual
	// slots will remain even when their containers are gone, so we need a failsafe for when that happens.

	/** Returns true if this slot is valid, i.e. the tile entity still exists. */
	public boolean isValid(){
		return !tileEntity.isRemoved();
	}

	// Normally the container decides if a stack is valid, but that's not going to work here
	@Override
	public boolean mayPlace(ItemStack stack){
		// getSlotIndex() is required here, NOT slotNumber (which is only populated when the slot is added to a
		// container - the javadoc is wrong, it has nothing to do with inventories)
		return isValid() && container.canPlaceItem(getSlotIndex(), stack);
	}

	@Override
	public boolean mayPickup(Player playerIn){
		return isValid() && super.mayPickup(playerIn);
	}

	@Override
	public void onTake(Player player, ItemStack stack){
		if(isValid()) {
			super.onTake(player, stack);
		}
	}

	@Override
	public void setChanged(){
		super.setChanged();
		if(this.hasItem()) this.prevStack = this.getItem().copy(); // Ignore stack removal (insertion of empty stacks)
	}

	@Override
	public ItemStack getItem(){
		return isValid() ? super.getItem() : ItemStack.EMPTY;
	}

	/** Returns the stack that was last in this slot. */
	public ItemStack getPrevStack(){
		return prevStack;
	}

	@Override
	public void set(ItemStack stack){
		if(isValid()){
			if(container instanceof TileEntityBookshelf) ((TileEntityBookshelf)container).sync();
			super.set(stack);
		}
	}

	@Override
	public ItemStack remove(int amount){
		if(isValid() && container instanceof TileEntityBookshelf) ((TileEntityBookshelf)container).sync();
		return isValid() ? super.remove(amount) : ItemStack.EMPTY;
	}

}
