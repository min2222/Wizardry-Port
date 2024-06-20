package electroblob.wizardry.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemManaFlask;
import electroblob.wizardry.misc.RecipeRechargeWithFlask;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for defining and registering wizardry's non-JSON recipes (i.e. smelting recipes and dynamic
 * crafting recipes). Also handles dynamic recipe display and usage.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
@Mod.EventBusSubscriber
public final class WizardryRecipes {

	private WizardryRecipes(){} // No instances!

	private static final List<Item> chargeableItems = new ArrayList<>();

	private static boolean registered;

	/** Adds the given item to the list of items that can be charged using mana flasks. Dynamic charging recipes
	 * will be added for these items during {@code RegistryEvent.Register<IRecipe>}. The item must implement
	 * {@link IManaStoringItem} for the recipes to work correctly. This method should be called from the item's
	 * constructor. */
	public static void addToManaFlaskCharging(Item item){

		if(registered){
			Wizardry.logger.warn("Tried to add an item to mana flask charging after it was registered, this will do nothing!");
			return;
		}

		chargeableItems.add(item);
	}

	/** Returns an unmodifiable view of all registered items that can be charged with mana flasks. */
	public static List<Item> getChargeableItems(){
		return Collections.unmodifiableList(chargeableItems);
	}

	/** Now only deals with the dynamic crafting recipes and the smelting recipes. */
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event){

		IForgeRegistry<IRecipe> registry = event.getRegistry();

		FurnaceRecipes.instance().addSmeltingRecipeForBlock(WizardryBlocks.CRYSTAL_ORE.get(), new ItemStack(WizardryItems.MAGIC_CRYSTAL.get()), 0.5f);

		// Mana flask recipes

		for(Item chargeable : chargeableItems){

			registry.register(new RecipeRechargeWithFlask(new ResourceLocation(Wizardry.MODID, "recipes/small_flask_" + ForgeRegistries.ITEMS.getKey(chargeable).getPath()), 
					chargeable, (ItemManaFlask)WizardryItems.SMALL_MANA_FLASK.get()));

			registry.register(new RecipeRechargeWithFlask(new ResourceLocation(Wizardry.MODID, "recipes/medium_flask_" + ForgeRegistries.ITEMS.getKey(chargeable).getPath()),
					chargeable, (ItemManaFlask)WizardryItems.MEDIUM_MANA_FLASK.get()));

			registry.register(new RecipeRechargeWithFlask(new ResourceLocation(Wizardry.MODID, "recipes/large_flask_" + ForgeRegistries.ITEMS.getKey(chargeable).getPath()),
					chargeable, (ItemManaFlask)WizardryItems.LARGE_MANA_FLASK.get()));

		}

		registered = true;

	}

}
