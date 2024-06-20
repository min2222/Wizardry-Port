package electroblob.wizardry.spell;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class ConjureArmour extends SpellConjuration {
	
	private static final Map<EquipmentSlot, Item> SPECTRAL_ARMOUR_MAP = ImmutableMap.of(
			EquipmentSlot.HEAD, WizardryItems.SPECTRAL_HELMET.get(),
			EquipmentSlot.CHEST, WizardryItems.SPECTRAL_CHESTPLATE.get(),
			EquipmentSlot.LEGS, WizardryItems.SPECTRAL_LEGGINGS.get(),
			EquipmentSlot.FEET, WizardryItems.SPECTRAL_BOOTS.get());

	public ConjureArmour(){
		super("conjure_armour", null);
	}
	
	@Override
	protected boolean conjureItem(Player caster, SpellModifiers modifiers){
		
		ItemStack armour;
		boolean flag = false;

		// Used this rather than getArmorInventoryList because I need to access the slot itself
		for(EquipmentSlot slot : InventoryUtils.ARMOUR_SLOTS){
			
			if(caster.getItemBySlot(slot).isEmpty() &&
					!InventoryUtils.doesPlayerHaveItem(caster, SPECTRAL_ARMOUR_MAP.get(slot))){
				
				armour = new ItemStack(SPECTRAL_ARMOUR_MAP.get(slot));
				IConjuredItem.setDurationMultiplier(armour, modifiers.get(WizardryItems.DURATION_UPGRADE.get()));
				// Sets a blank "ench" tag to trick the renderer into showing the enchantment effect on the armour model
				NBTExtras.storeTagSafely(armour.getTag(), "ench", new ListTag());
				caster.setItemSlot(slot, armour);
				flag = true;
			}
		}
		
		return flag;
	}

}
