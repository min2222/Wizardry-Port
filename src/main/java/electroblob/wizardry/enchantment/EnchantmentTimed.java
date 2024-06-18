package electroblob.wizardry.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

// This one is for everything other than imbued swords.
public class EnchantmentTimed extends Enchantment implements Imbuement {

	public EnchantmentTimed(){
		// Setting enchantment type to null stops the book appearing in the creative inventory
		super(Enchantment.Rarity.COMMON, null, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
	}

	@Override
	public boolean canEnchant(ItemStack p_92089_1_){
		return false;
	}

	/**
	 * Returns the maximum level that the enchantment can have.
	 */
	// Here, enchantment level is the damage multiplier of the spell used to apply the enchantment, i.e. with an
	// non-sorcerer wand it is level 1, an apprentice sorcerer wand is level 2, and so on. Note that basic sorcerer
	// wands can't
	// cast the imbue weapon spell, so level 2 is actually for apprentice wands.
	@Override
	public int getMaxLevel(){
		return 4;
	}

	@Override
	public boolean isAllowedOnBooks(){
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack){
		return false;
	}

}
