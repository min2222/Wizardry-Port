package electroblob.wizardry.enchantment;

import java.util.Iterator;

import com.google.common.collect.Iterables;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.ImbueWeapon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Interface for temporary enchantments that last for a certain duration ('imbuements'). This interface allows
 * {@link EnchantmentMagicSword} and {@link EnchantmentTimed} to both be treated as instances of a single type, rather
 * than having to deal with each of them separately, which would be inefficient and cumbersome (the former of those
 * classes cannot extend the latter because they both need to extend different subclasses of
 * {@link Enchantment}).
 *
 * @since Wizardry 1.2
 */
@Mod.EventBusSubscriber
public interface Imbuement {

	// This interface has no abstract methods, these handlers are just here for the sake of keeping things organised.

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// Instantly disenchants an imbued weapon if it is dropped when the player dies.
		for(ItemEntity item : event.getDrops()){
			// Apparently some mods don't behave and shove null items in the list, quite why I have no idea
			if(item != null) removeImbuements(item.getItem());
		}
	}

	@SubscribeEvent
	public static void onItemTossEvent(ItemTossEvent event){
		// Instantly disenchants an imbued weapon if it is thrown on the ground.
		removeImbuements(event.getEntity().getItem());
	}

	/** Removes all imbuements from the given itemstack. */
	static void removeImbuements(ItemStack stack){
		if(stack.isEnchanted()){
			// No need to check what enchantments the item has, since remove() does nothing if the element does not exist
			ListTag enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
					EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags();
			// Check all enchantments of the item
			Iterator<Tag> enchantmentIt = enchantmentList.iterator();
			while(enchantmentIt.hasNext()){
				CompoundTag enchantmentTag = (CompoundTag) enchantmentIt.next();
				Enchantment enchantment = Enchantment.byId(enchantmentTag.getShort("id"));
				// If the item contains a magic weapon enchantment, remove it from the item
				if(enchantment instanceof Imbuement){
					((Imbuement) enchantment).onImbuementRemoval(stack);
					enchantmentIt.remove();
				}
			}
		}
	}

	/** Allows executing some custom logic before this imbuement is being removed from the stack. Called when the imbuement is about to be removed. */
	default void onImbuementRemoval(ItemStack stack){}

	@SubscribeEvent
	public static void onPlayerOpenContainerEvent(PlayerContainerEvent event){
		// Brute-force fix to stop enchanted books in dungeon chests from having imbuements on them.
		if(event.getContainer() instanceof ChestMenu){
			// Still not sure if it's better to set stacks in slots or modify the itemstack list directly, but I would
			// imagine it's the former.
			for(Slot slot : event.getContainer().slots){
				ItemStack slotStack = slot.getItem();
				if(slotStack.getItem() instanceof EnchantedBookItem){
					// We don't care about the level of the enchantments
					ListTag enchantmentList = EnchantedBookItem.getEnchantments(slotStack);
					// Removes all imbuements
					if(Iterables.removeIf(enchantmentList, tag -> {
						CompoundTag enchantmentTag = (CompoundTag) tag;
						return Enchantment.byId(enchantmentTag.getShort("id"))
								instanceof Imbuement;
					})){
						// If any imbuements were removed, inform about the removal of the enchantment(s), or
						// delete the book entirely if there are none left.
						if(enchantmentList.isEmpty()){
							slot.set(ItemStack.EMPTY);
							Wizardry.logger.info("Deleted enchanted book with illegal enchantments");
						}else{
							// Inform about enchantment removal
							Wizardry.logger.info("Removed illegal enchantments from enchanted book");
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event){
		// Rather long-winded (but necessary) way of getting an arrow just after it has been fired, checking if the bow
		// that fired it has the imbuement enchantment, and applying extra damage accordingly.
		if(!event.getEntity().level.isClientSide && event.getEntity() instanceof Arrow){

			Arrow arrow = (Arrow)event.getEntity();

			if(arrow.getOwner() instanceof LivingEntity){

				LivingEntity archer = (LivingEntity)arrow.getOwner();

				ItemStack bow = archer.getMainHandItem();

				if(!ImbueWeapon.isBow(bow)){
					bow = archer.getOffhandItem();
					if(!ImbueWeapon.isBow(bow)) return;
				}

				// Taken directly from ItemBow, so it works exactly the same as the power enchantment.
				int level = bow.getEnchantmentLevel(WizardryEnchantments.MAGIC_BOW.get());

				if(level > 0){
					arrow.setBaseDamage(arrow.getBaseDamage() + (double)level * 0.5D + 0.5D);
				}

				if(bow.getEnchantmentLevel(WizardryEnchantments.FLAMING_WEAPON.get()) > 0){
					// Again, this is exactly what happens in ItemBow (flame is flame; level does nothing).
					arrow.setSecondsOnFire(100);
				}

				level = bow.getEnchantmentLevel(WizardryEnchantments.FREEZING_WEAPON.get());

				if(level > 0){
					if(arrow.getPersistentData() != null){
						arrow.getPersistentData().putInt(FreezingWeapon.FREEZING_ARROW_NBT_KEY, level);
					}
				}
			}
		}
	}

}
