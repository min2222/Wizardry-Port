package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.enchantment.EnchantmentMagicProtection;
import electroblob.wizardry.enchantment.EnchantmentMagicSword;
import electroblob.wizardry.enchantment.EnchantmentTimed;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for defining, storing and registering all of wizardry's enchantments.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryEnchantments {

	private WizardryEnchantments(){} // No instances!

	// At the moment these enchantments generate on books in dungeon chests due to a bad bit of code (EnchantRandomly:49).
	// No idea how to fix this because I have no way of hooking into that code... removing the enchantments from the
	// registry works, but breaks everything else!

	// For the time being, a dynamic solution will have to do, i.e. intercept the book when it is generated and
	// reassign its enchantment.

	// All of these have custom classes, so the unlocalised name (referred to simply as 'name' for enchantments) is
	// dealt with inside those classes.
	
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Wizardry.MODID);
	
    public static final RegistryObject<Enchantment> MAGIC_SWORD = ENCHANTMENTS.register("magic_sword", () -> new EnchantmentMagicSword());
    public static final RegistryObject<Enchantment> MAGIC_BOW = ENCHANTMENTS.register("magic_bow", () -> new EnchantmentTimed());
    public static final RegistryObject<Enchantment> FLAMING_WEAPON = ENCHANTMENTS.register("flaming_weapon", () -> new EnchantmentTimed());
    public static final RegistryObject<Enchantment> FREEZING_WEAPON = ENCHANTMENTS.register("freezing_weapon", () -> new EnchantmentTimed());

	public static final RegistryObject<Enchantment> MAGIC_PROTECTION = ENCHANTMENTS.register("magic_protection", () -> new EnchantmentMagicProtection(Enchantment.Rarity.UNCOMMON, EnchantmentMagicProtection.Type.MAGIC, InventoryUtils.ARMOUR_SLOTS));
	public static final RegistryObject<Enchantment> FROST_PROTECTION = ENCHANTMENTS.register("frost_protection", () -> new EnchantmentMagicProtection(Enchantment.Rarity.RARE, EnchantmentMagicProtection.Type.FROST, InventoryUtils.ARMOUR_SLOTS));
	public static final RegistryObject<Enchantment> SHOCK_PROTECTION = ENCHANTMENTS.register("shock_protection", () -> new EnchantmentMagicProtection(Enchantment.Rarity.RARE, EnchantmentMagicProtection.Type.SHOCK, InventoryUtils.ARMOUR_SLOTS));
}
