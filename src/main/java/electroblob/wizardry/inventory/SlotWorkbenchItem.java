package electroblob.wizardry.inventory;

import electroblob.wizardry.item.IWorkbenchItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The central wand/armour/scroll slot in the arcane workbench GUI.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotWorkbenchItem extends Slot {

	private ContainerArcaneWorkbench container;

	public SlotWorkbenchItem(Container inventory, int index, int x, int y, ContainerArcaneWorkbench container){
		super(inventory, index, x, y);
		this.container = container;
	}

	@Override
	public void set(ItemStack stack){
		super.set(stack);
		this.container.onSlotChanged(index, stack, null);
	}

	@Override
	public void onTake(Player player, ItemStack stack){
		this.container.onSlotChanged(index, ItemStack.EMPTY, player);
	}

	@Override
	public int getMaxStackSize(){
		return 16;
	}

	@Override
	public boolean mayPlace(ItemStack stack){
		return stack.getItem() instanceof IWorkbenchItem && ((IWorkbenchItem)stack.getItem()).canPlace(stack);
	}
}
