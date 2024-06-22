package electroblob.wizardry.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;

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
	//TODO
	/*@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event){
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

	}*/
	
	public static class WizardryRecipeGenerator extends RecipeProvider {
		public WizardryRecipeGenerator(DataGenerator p_125973_) {
			super(p_125973_);
		}
		
		@Override
		protected void buildCraftingRecipes(Consumer<FinishedRecipe> p_176532_) {
			
		}
	}
}
