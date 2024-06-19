package electroblob.wizardry.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Simple extension of {@link Slot} which only accepts items from an array of item classes defined in the constructor.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotItemClassList extends Slot {

	private final Class<? extends Item>[] itemClasses;
	private int stackLimit;

	@SafeVarargs
	public SlotItemClassList(Container inventory, int index, int x, int y, int stackLimit, Class<? extends Item>... allowedItemClasses){
		super(inventory, index, x, y);
		this.itemClasses = allowedItemClasses;
		this.stackLimit = stackLimit;
	}

	@Override
	public int getMaxStackSize(){
		return stackLimit;
	}

	@Override
	public boolean mayPlace(ItemStack stack){

		for(Class<? extends Item> itemClass : itemClasses){
			if(itemClass.isAssignableFrom(stack.getItem().getClass())){
				return true;
			}
		}

		return false;
	}
}
