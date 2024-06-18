package electroblob.wizardry.item;

import java.util.UUID;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellConjuration;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Allows wizardry to identify items that are conjured (and therefore need destroying if they leave the inventory)
 * without explicitly referencing each, thereby allowing for better expandability.
 */
@Mod.EventBusSubscriber
public interface IConjuredItem {

	/** The NBT tag key used to store the duration multiplier for conjured items. */
	String DURATION_MULTIPLIER_KEY = "durationMultiplier";
	/** The NBT tag key used to store the damage multiplier for conjured items. */
	String DAMAGE_MULTIPLIER = "damageMultiplier";

	UUID POTENCY_MODIFIER = UUID.fromString("da067ea6-0b35-4140-8436-5476224de9dd");

	/** Helper method for setting the duration multiplier (via NBT) for conjured items. */
	static void setDurationMultiplier(ItemStack stack, float multiplier){
		if(!stack.hasTag()) stack.setTag(new CompoundTag());
		stack.getTag().putFloat(DURATION_MULTIPLIER_KEY, multiplier);
	}

	/** Helper method for setting the damage multiplier (via NBT) for conjured items. */
	static void setDamageMultiplier(ItemStack stack, float multiplier){
		if(!stack.hasTag()) stack.setTag(new CompoundTag());
		stack.getTag().putFloat(DAMAGE_MULTIPLIER, multiplier);
	}

	/** Helper method for getting the damage multiplier (via NBT) for conjured items. */
	static float getDamageMultiplier(ItemStack stack){
		if(!stack.hasTag()) return 1;
		return stack.getTag().getFloat(DAMAGE_MULTIPLIER);
	}

	/**
	 * Helper method for returning the max damage of a conjured item based on its NBT data. Centralises the code.
	 * Implementors will almost certainly want to call this from {@link Item#getMaxDamage(ItemStack stack)}.
	 */
	default int getMaxDamageFromNBT(ItemStack stack, Spell spell){

		if(!spell.arePropertiesInitialised()) return 600; // Failsafe, some edge-cases call this during preInit

		float baseDuration = spell.getProperty(SpellConjuration.ITEM_LIFETIME).floatValue();

		if(stack.hasTag() && stack.getTag().contains(DURATION_MULTIPLIER_KEY)){
			return (int)(baseDuration * stack.getTag().getFloat(DURATION_MULTIPLIER_KEY));
		}

		return (int)baseDuration;
	}

	/**
	 * Adds property overrides to define the conjuring/vanishing animation. Call this from the item's constructor.
	 */
	default void addAnimationPropertyOverrides(){

		if(!(this instanceof Item)) throw new ClassCastException("Cannot set up conjuring animations for a non-item!");

		Item item = (Item)this;

		final int frames = getAnimationFrames();

		ItemProperties.register(item, new ResourceLocation("conjure"), (stack, p_174636_, p_174637_, p_174638_) -> {
			return stack.getDamageValue() < frames ? (float)stack.getDamageValue() / frames
					: (float)(stack.getMaxDamage() - stack.getDamageValue()) / frames;
		});
		ItemProperties.register(item, new ResourceLocation("conjuring"), (stack, p_174636_, p_174637_, p_174638_) -> {
			return stack.getDamageValue() < frames
					|| stack.getDamageValue() > stack.getMaxDamage() - frames ? 1.0F : 0.0F;
		});
	}

	/** Returns the number of frames in the conjuring/vanishing animation. Override to change the number of frames
	 * set by {@link IConjuredItem#addAnimationPropertyOverrides()}. */
	default int getAnimationFrames(){
		return 8;
	}

	/** Returns a blue colour to use for the durability bar for the given stack, so all items that use it as a timer
	 * have the same colours. */
	static int getTimerBarColour(ItemStack stack){
		return DrawingUtils.mix(0x8bffe0, 0x2ed1e4, (float)stack.getItem().getBarColor(stack));
	}

	@SubscribeEvent
	static void onLivingDropsEvent(LivingDropsEvent event){
		// Destroys conjured items if their caster dies.
		for(ItemEntity item : event.getDrops()){
			// Apparently some mods don't behave and shove null items in the list, quite why I have no idea
			if(item != null && item.getItem() != null && item.getItem().getItem() instanceof IConjuredItem){
				item.discard();
			}
		}
	}

	@SubscribeEvent
	static void onItemTossEvent(ItemTossEvent event){
		// Prevents conjured items being thrown by dragging and dropping outside the inventory.
		if(event.getEntity().getItem().getItem() instanceof IConjuredItem){
			event.setCanceled(true);
			event.getPlayer().getInventory().add(event.getEntity().getItem());
		}
	}
}
