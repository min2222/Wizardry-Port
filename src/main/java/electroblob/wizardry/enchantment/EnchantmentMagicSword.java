package electroblob.wizardry.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;

// This one is for imbued swords. The only reason this is separate is that the way vanilla is written allows me to hook
// into the damage increase for melee weapons, meaning I don't have to use events - always handy!
public class EnchantmentMagicSword extends DamageEnchantment implements Imbuement {

	public EnchantmentMagicSword(){
		super(Enchantment.Rarity.COMMON, 0, EquipmentSlot.MAINHAND);
		// Setting this to null stops the book appearing in the creative inventory
	}

	@Override
	public boolean canEnchant(ItemStack p_92089_1_){
		return false;
	}

	/**
	 * Returns the maximum level that the enchantment can have.
	 */
	// Here, enchantment level is the damage multiplier of the spell used to apply the enchantment, i.e. with an
	// non-sorcerer wand it is level 1, a basic sorcerer wand is level 2, and so on. Note that basic sorcerer wands
	// can't
	// cast the imbue weapon spell, so level 2 is actually for apprentice wands.
	@Override
	public int getMaxLevel(){
		return 4;
	}

	// Returns the number by which the damage should be increased (or something)
	@Override
	public float getDamageBonus(int p_152376_1_, MobType p_152376_2_){
		return (float)p_152376_1_ * 1.25F;
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
