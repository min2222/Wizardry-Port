package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockArcaneWorkbench;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.block.BlockCrystalFlower;
import electroblob.wizardry.block.BlockCrystalOre;
import electroblob.wizardry.block.BlockDryFrostedIce;
import electroblob.wizardry.block.BlockImbuementAltar;
import electroblob.wizardry.block.BlockLectern;
import electroblob.wizardry.block.BlockPermafrost;
import electroblob.wizardry.block.BlockSnare;
import electroblob.wizardry.block.BlockSpectral;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.block.BlockThorns;
import electroblob.wizardry.block.BlockTransportationStone;
import electroblob.wizardry.block.BlockVanishingCobweb;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import electroblob.wizardry.tileentity.TileEntityLectern;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.tileentity.TileEntityReceptacle;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.tileentity.TileEntityThorns;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for defining, storing and registering all of wizardry's blocks. Also handles registry of the
 * tile entities.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryBlocks {

	private WizardryBlocks(){} // No instances!

	// Found a very nice way of registering things using arrays, which might make @ObjectHolder actually useful.
	// http://www.minecraftforge.net/forum/topic/49497-1112-is-using-registryevent-this-way-ok/

	// Anything set to use the material 'air' will not render, even with a TESR!

	// setSoundType should be public, but in this particular version it isn't... which is a bit of a pain.
	
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Wizardry.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Wizardry.MODID);

    public static final RegistryObject<Block> ARCANE_WORKBENCH = BLOCKS.register("arcane_workbench", () -> new BlockArcaneWorkbench());
    public static final RegistryObject<Block> CRYSTAL_ORE = BLOCKS.register("crystal_ore", () -> new BlockCrystalOre());
    public static final RegistryObject<Block> CRYSTAL_FLOWER = BLOCKS.register("crystal_flower", () -> new BlockCrystalFlower());
	public static final RegistryObject<Block> TRANSPORTATION_STONE = BLOCKS.register("transportation_stone", () -> new BlockTransportationStone(BlockBehaviour.Properties.of(Material.STONE).destroyTime(0.3F).lightLevel(value -> (int)7.5f).randomTicks().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MAGIC_CRYSTAL_BLOCK = BLOCKS.register("magic_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_PINK).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> FIRE_CRYSTAL_BLOCK = BLOCKS.register("fire_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.TERRACOTTA_ORANGE).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ICE_CRYSTAL_BLOCK = BLOCKS.register("ice_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_BLUE).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> LIGHTNING_CRYSTAL_BLOCK = BLOCKS.register("lightning_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_CYAN).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> NECROMANCY_CRYSTAL_BLOCK = BLOCKS.register("necromancy_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_PURPLE).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> EARTH_CRYSTAL_BLOCK = BLOCKS.register("earth_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GREEN).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> SORCERY_CRYSTAL_BLOCK = BLOCKS.register("sorcery_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_LIGHT_GREEN).strength(5, 10).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HEALING_CRYSTAL_BLOCK = BLOCKS.register("healing_crystal_block", () -> new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_YELLOW).strength(5, 10).requiresCorrectToolForDrops()));

	public static final RegistryObject<Block> PETRIFIED_STONE = BLOCKS.register("petrified_stone", () -> new BlockStatue(BlockBehaviour.Properties.of(Material.STONE).strength(1.5F, 10.0F), false));
	public static final RegistryObject<Block> ICE_STATUE = BLOCKS.register("ice_statue", () -> new BlockStatue(BlockBehaviour.Properties.of(Material.ICE).strength(0.5F).friction(0.98F).sound(SoundType.GLASS).noOcclusion(), true));
	public static final RegistryObject<Block> MAGIC_LIGHT = placeholder();
	public static final RegistryObject<Block> SNARE = BLOCKS.register("snare", () -> new BlockSnare(BlockBehaviour.Properties.of(Material.PLANT).sound(SoundType.CROP).instabreak().noOcclusion()));
	public static final RegistryObject<Block> SPECTRAL_BLOCK = BLOCKS.register("spectral_block", () -> new BlockSpectral(BlockBehaviour.Properties.of(Material.GLASS).strength(-1.0F, 6000000.0F).sound(SoundType.GLASS)));
	public static final RegistryObject<Block> METEOR = BLOCKS.register("meteor", () -> new Block(BlockBehaviour.Properties.of(Material.STONE).lightLevel((state) -> 1)));
	public static final RegistryObject<Block> VANISHING_COBWEB = BLOCKS.register("vanishing_cobweb", () -> new BlockVanishingCobweb(BlockBehaviour.Properties.of(Material.WEB).noCollission().strength(4)));
    public static final RegistryObject<Block> THORNS = BLOCKS.register("thorns", () -> new BlockThorns());
	public static final RegistryObject<Block> OBSIDIAN_CRUST = placeholder();
	public static final RegistryObject<Block> DRY_FROSTED_ICE = BLOCKS.register("dry_frosted_ice", () -> new BlockDryFrostedIce());
	public static final RegistryObject<Block> CRYSTAL_FLOWER_POT = placeholder();
	public static final RegistryObject<Block> PERMAFROST = BLOCKS.register("permafrost", () -> new BlockPermafrost());

    public static final RegistryObject<Block> FIRE_RUNESTONE = BLOCKS.register("fire_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> ICE_RUNESTONE = BLOCKS.register("ice_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> LIGHTNING_RUNESTONE = BLOCKS.register("lightning_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> NECROMANCY_RUNESTONE = BLOCKS.register("necromancy_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> EARTH_RUNESTONE = BLOCKS.register("earth_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> SORCERY_RUNESTONE = BLOCKS.register("sorcery_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> HEALING_RUNESTONE = BLOCKS.register("healing_runestone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(1.5F, 10.0F)));

    public static final RegistryObject<Block> FIRE_RUNESTONE_PEDESTAL = BLOCKS.register("fire_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_RED).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> ICE_RUNESTONE_PEDESTAL = BLOCKS.register("ice_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_LIGHT_BLUE).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> LIGHTNING_RUNESTONE_PEDESTAL = BLOCKS.register("lightning_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_CYAN).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> NECROMANCY_RUNESTONE_PEDESTAL = BLOCKS.register("necromancy_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_PURPLE).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> EARTH_RUNESTONE_PEDESTAL = BLOCKS.register("earth_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BROWN).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> SORCERY_RUNESTONE_PEDESTAL = BLOCKS.register("sorcery_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_GRAY).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> HEALING_RUNESTONE_PEDESTAL = BLOCKS.register("healing_runestone_pedestal", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_YELLOW).strength(1.5F, 10.0F)));

    public static final RegistryObject<Block> GILDED_OAK_WOOD = BLOCKS.register("gilded_oak_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GILDED_SPRUCE_WOOD = BLOCKS.register("gilded_spruce_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GILDED_BIRCH_WOOD = BLOCKS.register("gilded_birch_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GILDED_JUNGLE_WOOD = BLOCKS.register("gilded_jungle_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GILDED_ACACIA_WOOD = BLOCKS.register("gilded_acacia_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    public static final RegistryObject<Block> GILDED_DARK_OAK_WOOD = BLOCKS.register("gilded_dark_oak_wood", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).strength(2, 5).sound(SoundType.WOOD)));
    
    public static final RegistryObject<Block> OAK_BOOKSHELF = BLOCKS.register("oak_bookshelf", () -> new BlockBookshelf());
    public static final RegistryObject<Block> SPRUCE_BOOKSHELF = BLOCKS.register("spruce_bookshelf", () -> new BlockBookshelf());
    public static final RegistryObject<Block> BIRCH_BOOKSHELF = BLOCKS.register("birch_bookshelf", () -> new BlockBookshelf());
    public static final RegistryObject<Block> JUNGLE_BOOKSHELF = BLOCKS.register("jungle_bookshelf", () -> new BlockBookshelf());
    public static final RegistryObject<Block> ACACIA_BOOKSHELF = BLOCKS.register("acacia_bookshelf", () -> new BlockBookshelf());
    public static final RegistryObject<Block> DARK_OAK_BOOKSHELF = BLOCKS.register("dark_oak_bookshelf", () -> new BlockBookshelf());

    public static final RegistryObject<Block> OAK_LECTERN = BLOCKS.register("oak_lectern", () -> new BlockLectern());
    public static final RegistryObject<Block> SPRUCE_LECTERN = BLOCKS.register("spruce_lectern", () -> new BlockLectern());
    public static final RegistryObject<Block> BIRCH_LECTERN = BLOCKS.register("birch_lectern", () -> new BlockLectern());
    public static final RegistryObject<Block> JUNGLE_LECTERN = BLOCKS.register("jungle_lectern", () -> new BlockLectern());
    public static final RegistryObject<Block> ACACIA_LECTERN = BLOCKS.register("acacia_lectern", () -> new BlockLectern());
    public static final RegistryObject<Block> DARK_OAK_LECTERN = BLOCKS.register("dark_oak_lectern", () -> new BlockLectern());

	public static final RegistryObject<Block> RECEPTACLE = placeholder();
    public static final RegistryObject<Block> IMBUEMENT_ALTAR = BLOCKS.register("imbuement_altar", () -> new BlockImbuementAltar());
    
    public static final RegistryObject<BlockEntityType<TileEntityArcaneWorkbench>> ARCANE_WORKBENCH_BLOCK_ENTITY = BLOCK_ENTITIES.register("arcane_workbench", () -> BlockEntityType.Builder.of(TileEntityArcaneWorkbench::new, WizardryBlocks.ARCANE_WORKBENCH.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityBookshelf>> BOOKSHELF_BLOCK_ENTITY = BLOCK_ENTITIES.register("bookshelf", 
    		() -> BlockEntityType.Builder.of(TileEntityBookshelf::new, 
    				WizardryBlocks.OAK_BOOKSHELF.get(), 
    				WizardryBlocks.SPRUCE_BOOKSHELF.get(), 
    				WizardryBlocks.BIRCH_BOOKSHELF.get(), 
    				WizardryBlocks.JUNGLE_BOOKSHELF.get(), 
    				WizardryBlocks.ACACIA_BOOKSHELF.get(), 
    				WizardryBlocks.DARK_OAK_BOOKSHELF.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityImbuementAltar>> IMBUEMENT_ALTAR_BLOCK_ENTITY = BLOCK_ENTITIES.register("imbuement_altar", () -> BlockEntityType.Builder.of(TileEntityImbuementAltar::new, WizardryBlocks.IMBUEMENT_ALTAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityLectern>> LECTERN_BLOCK_ENTITY = BLOCK_ENTITIES.register("lectern", 
    		() -> BlockEntityType.Builder.of(TileEntityLectern::new, 
    				WizardryBlocks.OAK_LECTERN.get(), 
    				WizardryBlocks.SPRUCE_LECTERN.get(), 
    				WizardryBlocks.BIRCH_LECTERN.get(), 
    				WizardryBlocks.JUNGLE_LECTERN.get(), 
    				WizardryBlocks.ACACIA_LECTERN.get(), 
    				WizardryBlocks.DARK_OAK_LECTERN.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityPlayerSave>> PLAYER_SAVE_BLOCK_ENTITY = BLOCK_ENTITIES.register("player_save", () -> BlockEntityType.Builder.of(TileEntityPlayerSave::new, WizardryBlocks.SNARE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityReceptacle>> RECEPTACLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("receptacle", () -> BlockEntityType.Builder.of(TileEntityReceptacle::new, WizardryBlocks.RECEPTACLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityStatue>> STATUE_BLOCK_ENTITY = BLOCK_ENTITIES.register("statue", () -> BlockEntityType.Builder.of(TileEntityStatue::new, WizardryBlocks.PETRIFIED_STONE.get(), WizardryBlocks.ICE_STATUE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityThorns>> THORNS_BLOCK_ENTITY = BLOCK_ENTITIES.register("thorns", () -> BlockEntityType.Builder.of(TileEntityThorns::new, WizardryBlocks.THORNS.get()).build(null));
    public static final RegistryObject<BlockEntityType<TileEntityTimer>> TIMER_BLOCK_ENTITY = BLOCK_ENTITIES.register("timer", () -> BlockEntityType.Builder.of(TileEntityTimer::new, WizardryBlocks.VANISHING_COBWEB.get()).build(null));
}