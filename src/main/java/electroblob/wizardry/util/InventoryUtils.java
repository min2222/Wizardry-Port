package electroblob.wizardry.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains useful static methods for interacting with item stacks and entity inventories. These methods used to be part
 * of {@code WizardryUtilities}.
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class InventoryUtils {

	private InventoryUtils(){} // No instances!

	/** Constant which is simply an array of the four armour slots. (Could've sworn this exists somewhere in vanilla,
	 * but I can't find it anywhere...) */
	public static final EquipmentSlot[] ARMOUR_SLOTS;

	static {
		// The list of slots needs to be mutable.
		List<EquipmentSlot> slots = new ArrayList<>(Arrays.asList(EquipmentSlot.values()));
		slots.removeIf(slot -> slot.getSlotType() != Type.ARMOR);
		ARMOUR_SLOTS = slots.toArray(new EquipmentSlot[0]);
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar. Defined here for convenience and to centralise the
	 * (unfortunately unavoidable) use of hardcoded numbers to reference the inventory slots. The returned list is a
	 * modifiable copy of part of the player's inventory stack list; as such, changes to the list are <b>not</b> written
	 * through to the player's inventory. However, the ItemStack instances themselves are not copied, so changes to any
	 * of their fields (size, metadata...) will change those in the player's inventory.
	 *
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getHotbar(Player player){
		NonNullList<ItemStack> hotbar = NonNullList.create();
		hotbar.addAll(player.inventory.mainInventory.subList(0, 9));
		return hotbar;
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar and offhand, sorted into the following order: main
	 * hand, offhand, rest of hotbar left-to-right. The returned list is a modifiable shallow copy of part of the player's
	 * inventory stack list; as such, changes to the list are <b>not</b> written through to the player's inventory.
	 * However, the ItemStack instances themselves are not copied, so changes to any of their fields (size, metadata...)
	 * will change those in the player's inventory.
	 *
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getPrioritisedHotbarAndOffhand(Player player){
		List<ItemStack> hotbar = getHotbar(player);
		// Adds the offhand item to the beginning of the list so it is processed before the hotbar
		hotbar.add(0, player.getItemInHandOffhand());
		// Moves the item in the main hand to the beginning of the list so it is processed first
		hotbar.remove(player.getMainHandItem());
		hotbar.add(0, player.getMainHandItem());
		return hotbar;
	}

	/** Returns which {@link EnumHandSide} the given {@link InteractionHand} is on for the given entity. */
	public static EnumHandSide getSideForHand(LivingEntity entity, InteractionHand hand){
		return hand == InteractionHand.MAIN_HAND ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite();
	}

	/** Returns which {@link InteractionHand} is on the given {@link EnumHandSide} for the given entity. */
	public static InteractionHand getHandForSide(LivingEntity entity, EnumHandSide side){
		return side == entity.getPrimaryHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}

	/** Returns the opposite {@link InteractionHand} to the one given. */
	public static InteractionHand getOpposite(InteractionHand hand){
		return hand == InteractionHand.OFF_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}

	/**
	 * Tests whether the specified player has any of the specified item in their entire inventory, including armour
	 * slots and offhand.
	 */
	public static boolean doesPlayerHaveItem(Player player, Item item){

		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.armorInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.offHandInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a new {@link ItemStack} that is identical to the supplied one, except with the metadata changed to the
	 * new value given.
	 * @param toCopy The stack to copy
	 * @param newMetadata The new metadata value
	 * @return The resulting {@link ItemStack}
	 */
	public static ItemStack copyWithMeta(ItemStack toCopy, int newMetadata){
		ItemStack copy = new ItemStack(toCopy.getItem(), toCopy.getCount(), newMetadata);
		CompoundTag compound = toCopy.getTag();
		if(compound != null) copy.setTag(compound.copy());
		return copy;
	}

	/**
	 * Returns whether the two given item stacks can be merged, i.e. if they both contain the same (stackable) item,
	 * metadata and NBT. Importantly, the number of items in each stack need not be the same. No actual merging is
	 * performed by this method; the input stacks will not be modified.
	 * @param stack1 The first stack to be tested for mergeability
	 * @param stack2 The second stack to be tested for mergeability (order does not matter)
	 * @return True if the two stacks can be merged, false if not
	 */
	public static boolean canMerge(ItemStack stack1, ItemStack stack2){
		return !stack1.isEmpty() && !stack2.isEmpty()
				&& stack1.isStackable() && stack2.isStackable()
				&& stack1.getItem() == stack2.getItem()
				&& (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	/**
	 * A version of {@link Entity#replaceItemInInventory(int, ItemStack)} that takes a slot index specific to the main,
	 * armour and offhand inventories, as passed to {@link Item#tick(ItemStack, Level, Entity, int, boolean)},
	 * rather than the proper slot index used everywhere else (just <i>why</i>, Mojang?).
	 * @param entity The entity to replace the item for
	 * @param slot The slot index to replace
	 * @param original The item stack currently in the slot (required for technical reasons)
	 * @param replacement The new item stack
	 * @return True if an item was replaced, false if not
	 */
	public static boolean replaceItemInInventory(Entity entity, int slot, ItemStack original, ItemStack replacement){
		// Check slots that aren't in the main inventory first by comparing with the existing item
		if(entity instanceof LivingEntity){
			for(EquipmentSlot eslot : EquipmentSlot.values()){
				if(((LivingEntity)entity).getItemStackFromSlot(eslot) == original){
					entity.setItemStackToSlot(eslot, replacement);
					return true;
				}
			}
		}
		// Otherwise use the normal behaviour
		return entity.replaceItemInInventory(slot, replacement);
	}

}
