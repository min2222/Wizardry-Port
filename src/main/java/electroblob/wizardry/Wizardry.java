package electroblob.wizardry;

import java.io.File;
import java.util.Calendar;

import org.apache.logging.log4j.Logger;

import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.command.CommandCastSpell;
import electroblob.wizardry.command.CommandDiscoverSpell;
import electroblob.wizardry.command.CommandSetAlly;
import electroblob.wizardry.command.CommandViewAllies;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.integration.baubles.WizardryBaublesIntegration;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.misc.DonationPerksHandler;
import electroblob.wizardry.misc.Forfeit;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryEntities;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryLoot;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellNetworkIDSorter;
import electroblob.wizardry.util.SpellProperties;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.worldgen.WorldGenCrystalFlower;
import electroblob.wizardry.worldgen.WorldGenCrystalOre;
import electroblob.wizardry.worldgen.WorldGenLibraryRuins;
import electroblob.wizardry.worldgen.WorldGenObelisk;
import electroblob.wizardry.worldgen.WorldGenShrine;
import electroblob.wizardry.worldgen.WorldGenUndergroundLibraryRuins;
import electroblob.wizardry.worldgen.WorldGenWizardTower;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;

/**
 * <i>"Electroblob's Wizardry adds an RPG-like system of spells to Minecraft, with the aim of being as playable as
 * possible. No crazy constructs, no perk trees, no complex recipes - simply find spell books, cast spells, and master
 * the arcane! - But you knew that, right?"</i>
 * <p></p>
 * Main mod class for Wizardry. Contains the logger and settings instances, along with all the other stuff that's normally
 * in a main mod class.
 * @author Electroblob
 * @since Wizardry 1.0
 */

@Mod(Wizardry.MODID)
public class Wizardry {

	/** Wizardry's mod ID. */
	public static final String MODID = "ebwizardry";
	/** Wizardry's mod name, in readable form. */
	public static final String NAME = "Electroblob's Wizardry";
	/**
	 * The version number for this version of wizardry. The following system is used for version numbers:
	 * <p></p>
	 * <center><b>[major Minecraft version].[major mod version].[minor mod version/patch]</b>
	 * <p></p>
	 * </center> The major mod version is consistent across Minecraft versions, i.e. Wizardry 1.1 has the same
	 * features as Wizardry 2.1, but they are for different versions of Minecraft and have separate minor versioning.
	 * 1.x.x represents Minecraft 1.7.x versions, 2.x.x represents Minecraft 1.10.x versions, 3.x.x represents Minecraft
	 * 1.11.x versions, and so on.
	 */
	public static final String VERSION = "4.3.14";

	/**
	 * Json file used by Forge's built-in <a href="https://mcforge.readthedocs.io/en/1.12.x/gettingstarted/autoupdate/">update checker</a>.
	 * Used by the in-game mod menu to show that green icon whenever a newer version is available of the mod.
	 */
	public static final String updateJSON = "https://github.com/Electroblob77/Wizardry/blob/1.12.2/.forge/update.json";

	// IDEA: Triggering of inbuilt Forge events in relevant places?
	// IDEA: Abstract the vanilla particles behind the particle builder

	// TODO: Have particles obey Minecraft's particle setting where appropriate
	// (see https://github.com/RootsTeam/Embers/blob/master/src/main/java/teamroots/embers/particle/ParticleUtil.java)
	// TODO: TileEntityArcaneWorkbench needs looking at, esp. regarding inventory and markDirty

	/** Static instance of the {@link Settings} object for Wizardry. */
	public static final Settings settings = new Settings();

	/** Static instance of the {@link Logger} object for Wizardry.
	 * <p></p>
	 * Logging conventions for wizardry (only these levels are used currently):
	 * <p></p>
	 * - <b>ERROR</b>: Anything that threw an exception; may or may not crash the game.<br>
	 * - <b>WARN</b>: Anything that isn't supposed to happen during normal operation, but didn't throw an exception.<br>
	 * - <b>INFO</b>: Anything that might happen during normal mod operation that the user needs to know about. */
	public static Logger logger;

	/** A {@link File} object representing wizardry's config folder, {@code config/ebwizardry}). As of wizardry 4.2.4,
	 * this folder contains the main config file and the global spell properties folder, if used. */
	public static File configDirectory;

	public static boolean tisTheSeason;

	// Location of the proxy code, used by Forge.
	public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
	
	public Wizardry() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		WizardryEntities.ENTITY_TYPES.register(bus);
		WizardryPotions.EFFECTS.register(bus);
		WizardryItems.ITEMS.register(bus);
		WizardryItems.PATTERNS.register(bus);
		WizardryBlocks.BLOCKS.register(bus);
		WizardryBlocks.BLOCK_ENTITIES.register(bus);
		WizardryGuiHandler.MENUS.register(bus);
		WizardryLoot.FUNCTIONS.register(bus);
		WizardryEnchantments.ENCHANTMENTS.register(bus);
		bus.addListener(this::commonSetup);
	}

    private void commonSetup(FMLCommonSetupEvent event) {

		logger = event.getModLog();

		configDirectory = new File(event.getModConfigurationDirectory(), Wizardry.MODID);
		settings.initConfig(event);

		Calendar calendar = Calendar.getInstance();
		tisTheSeason = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DAY_OF_MONTH) >= 24
				&& calendar.get(Calendar.DAY_OF_MONTH) <= 26;

		// Capabilities
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, WizardData::attachCapability);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, DispenserCastingData::attachCapability);

		// Register things that don't have registries
		WizardryLoot.register();
		WizardryAdvancementTriggers.register();
		Forfeit.register();
		BlockBookshelf.registerStandardBookModelTextures();

		// Client-side stuff (via proxies)
		proxy.registerRenderers();
		proxy.registerKeyBindings();

		// Commented out for 4.2.3 to get rid of the sound bug, reinstate once a fix is found.
		WizardrySounds.SPELLS = SoundSource.PLAYERS;//CustomSoundCategory.add(Wizardry.MODID + "_spells");

		WizardryBaublesIntegration.init();

	}

	@EventHandler
	public void init(FMLInitializationEvent event){
		proxy.registerResourceReloadListeners();

		settings.initConfigExtras();

		// World generators
		// Weight is a misnomer, it's actually the priority (where lower numbers get generated first)
		// Literally nothing on typical 'weight' values here, there isn't even an upper limit
		// Examples I've managed to find:
		// - Tinker's construct slime islands use 25
		GameRegistry.registerWorldGenerator(new WorldGenCrystalOre(), 0);
		GameRegistry.registerWorldGenerator(new WorldGenCrystalFlower(), 50);
		GameRegistry.registerWorldGenerator(new WorldGenWizardTower(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenObelisk(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenShrine(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenLibraryRuins(), 20);
		GameRegistry.registerWorldGenerator(new WorldGenUndergroundLibraryRuins(), 20);

		// This is for the config change and missing mappings events
		MinecraftForge.EVENT_BUS.register(instance); // Since there's already an instance we might as well use it

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new WizardryGuiHandler());
		WizardryPacketHandler.initPackets();

		// Post-registry extras
		BlockBookshelf.compileBookModelTextures();
		ContainerBookshelf.initDefaultBookItems();
		WizardryItems.registerDispenseBehaviours();
		WizardryItems.registerBannerPatterns();
		WandHelper.populateUpgradeMap();
		Spell.registry.forEach(Spell::init);
		SpellProperties.init();

		// Client-side stuff (via proxies)
		proxy.initGuiBits();
		proxy.registerParticles();
		proxy.registerSoundEventListener();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		proxy.initialiseLayers();
		proxy.initialiseAnimations();

		SpellNetworkIDSorter.init();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new CommandCastSpell());
		event.registerServerCommand(new CommandSetAlly());
		event.registerServerCommand(new CommandViewAllies());
		event.registerServerCommand(new CommandDiscoverSpell());
	}

	@SubscribeEvent
	public void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.getModID().equals(Wizardry.MODID)){
			settings.saveConfigChanges();
			// All of the synchronised settings require a world restart anyway so don't need syncing, except for the
			// donor perks which have special behaviour (since we have to update everyone when a donor logs in anyway,
			// we might as well reuse the packet and let them change the setting mid-game)
			if(event.isWorldRunning() && DonationPerksHandler.isDonor(proxy.getThePlayer())){
				DonationPerksHandler.sendToServer(proxy.getThePlayer());
			}
		}
	}

}