package electroblob.wizardry.registry;

import java.util.Arrays;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.loot.RandomSpell;
import electroblob.wizardry.loot.WizardSpell;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for registering wizardry's loot functions and loot tables. Also handles loot injection and the
 * standard weighting.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public final class WizardryLoot {

	//public static final String FROM_SPAWNER_NBT_FLAG = "fromSpawner";
	
    public static final DeferredRegister<LootItemFunctionType> FUNCTIONS = DeferredRegister.create(Registry.LOOT_FUNCTION_REGISTRY, Wizardry.MODID);

    public static final RegistryObject<LootItemFunctionType> RANDOM_SPELL = FUNCTIONS.register("random_spell", () -> new LootItemFunctionType(new RandomSpell.Serializer()));
    public static final RegistryObject<LootItemFunctionType> WIZARD_SPELL = FUNCTIONS.register("wizard_spell", () -> new LootItemFunctionType(new WizardSpell.Serializer()));

	public static final ResourceLocation[] RUINED_SPELL_BOOK_LOOT_TABLES = Arrays.stream(Element.values())
			.filter(e -> e != Element.MAGIC)
			.map(e -> new ResourceLocation(Wizardry.MODID, "gameplay/imbuement_altar/ruined_spell_book_" + e.getName()))
			.toArray(ResourceLocation[]::new);

	private WizardryLoot(){} // No instances!

	/** Called from the preInit method in the main mod class to register the custom dungeon loot. */
	public static void register(){

		/* Loot tables work as follows: Minecraft goes through each pool in turn. For each pool, it does a certain
		 * number of rolls, which can either be set to always be one number or a random number from a range. Each roll,
		 * it generates one stack of a single random entry in that pool, weighted according to the weights of the
		 * entries. Functions allow properties of that stack (stack size, damage, nbt) to be set, and even allow it to
		 * be replaced dynamically with a completely different item (though there's very little point in doing that as
		 * it could be achieved just as easily with more entries, which makes me think it would be bad practice). You
		 * can also use conditions to control whether an entry or pool is used at all, which is mostly for mob drops
		 * under specific conditions, but one of them is simply a random chance, meaning you could use it to make a pool
		 * that only gets rolled sometimes. All in all, this can get rather confusing, because stackable items can have
		 * 5 stages of randomness applied to them at once: a random chance for the pool, a random number of rolls for
		 * the pool, the weighted random chance of choosing that particular entry, a random chance for that entry, and a
		 * random stack size, and that's before you take functions into account.
		 * 
		 * ...oh, and entries can be entire loot tables in themselves, allowing for potentially infinite levels of
		 * randomness. Yeah. */
 
		// Always registers the loot tables, but only injects the additions into vanilla if the appropriate option is
		// enabled in the config (see WizardryEventHandler).
		//TODO
		/*LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/wizard_tower"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/obelisk"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/shrine"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/dungeon_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/jungle_dispenser_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/elemental_crystals"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wizard_armour"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/arcane_tomes"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wand_upgrades"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/uncommon_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/rare_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/epic_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/evil_wizard"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/mob_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "gameplay/fishing/junk_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "gameplay/fishing/treasure_additions"));
		for(ResourceLocation location : RUINED_SPELL_BOOK_LOOT_TABLES) LootTableList.register(location);*/

	}

	@SubscribeEvent
	public static void onLootTableLoadEvent(LootTableLoadEvent event){
		// General dungeon loot
		if(Arrays.asList(Wizardry.settings.lootInjectionLocations).contains(event.getName())){
			event.getTable().addPool(getAdditive(Wizardry.MODID + ":chests/dungeon_additions", Wizardry.MODID + "_additional_dungeon_loot"));
		}
		// Jungle temple dispensers
		if(event.getName().toString().matches("minecraft:chests/jungle_temple_dispenser")){
			event.getTable().addPool(getAdditive(Wizardry.MODID + ":chests/jungle_dispenser_additions", Wizardry.MODID + "_additional_dispenser_loot"));
		}
		// Mob drops
		if (Wizardry.settings.injectMobDrops) {
			// Let's hope mods will play nice and store their entity loot tables under 'entities' or 'entity'
			// If not, packmakers will have to sort it out themselves using the whitelist/blacklist
			if (Arrays.asList(Wizardry.settings.mobLootTableWhitelist).contains(event.getName())) {
				event.getTable().addPool(getAdditive(Wizardry.MODID + ":entities/mob_additions", Wizardry.MODID + "_additional_mob_drops"));

			} else if (!Arrays.asList(Wizardry.settings.mobLootTableBlacklist).contains(event.getName())
					&& event.getName().getPath().contains("entities") || event.getName().getPath().contains("entity")) {
				// Get the filename of the loot table json, for well-behaved mods this will be the entity name
				String[] split = event.getName().getPath().split("/");
				String entityName = split[split.length - 1];

				EntityType<?> entry = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityName));
				if (entry == null) {
					return; // If this is true it didn't work :(
				}

				if (entry.getCategory().equals(MobCategory.MONSTER)) {
					event.getTable().addPool(getAdditive(Wizardry.MODID + ":entities/mob_additions", Wizardry.MODID + "_additional_mob_drops"));
				}
			}
		}
		// Fishing loot
		// This works slightly differently in that it modifies the existing pools rather than adding new ones
		// This is because you are supposed to only catch one item at a time!
		if(event.getName().toString().matches("minecraft:gameplay/fishing/junk")){
			// The first (and in this case, only) vanilla loot pool is named "main"
			event.getTable().addPool(LootPool.lootPool().add(getAdditiveEntry(Wizardry.MODID + ":gameplay/fishing/junk_additions", 4)).build());
		}else if(event.getName().toString().matches("minecraft:gameplay/fishing/treasure")){
			event.getTable().addPool(LootPool.lootPool().add(getAdditiveEntry(Wizardry.MODID + ":gameplay/fishing/treasure_additions", 1)).build());
		}
	}

    private static LootPool getAdditive(String entryName, String poolName) {
        return LootPool.lootPool().add(getAdditiveEntry(entryName, 1)).setRolls(ConstantValue.exactly(1)).setBonusRolls(UniformGenerator.between(0, 1)).name(Wizardry.MODID + "_" + poolName).build();
    }

    private static LootPoolEntryContainer.Builder<?> getAdditiveEntry(String name, int weight) {
        return LootTableReference.lootTableReference(new ResourceLocation(name)).setWeight(weight).setQuality(0);
    }

//	@SubscribeEvent
//	public static void onLivingSpawnEvent(LivingSpawnEvent.SpecialSpawn event){
//		if(event.getSpawner() != null) event.getEntity().getPersistentData().setBoolean(FROM_SPAWNER_NBT_FLAG, true);
//	}

}
