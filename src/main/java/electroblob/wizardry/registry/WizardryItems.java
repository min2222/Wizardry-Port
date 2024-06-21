package electroblob.wizardry.registry;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemBlankScroll;
import electroblob.wizardry.item.ItemFirebomb;
import electroblob.wizardry.item.ItemFlamecatcher;
import electroblob.wizardry.item.ItemFlamingAxe;
import electroblob.wizardry.item.ItemFrostAxe;
import electroblob.wizardry.item.ItemIdentificationScroll;
import electroblob.wizardry.item.ItemLightningHammer;
import electroblob.wizardry.item.ItemManaFlask;
import electroblob.wizardry.item.ItemPoisonBomb;
import electroblob.wizardry.item.ItemPurifyingElixir;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSmokeBomb;
import electroblob.wizardry.item.ItemSparkBomb;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemSpectralDust;
import electroblob.wizardry.item.ItemSpectralPickaxe;
import electroblob.wizardry.item.ItemSpectralSword;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWandUpgrade;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.item.ItemWizardArmour.ArmourClass;
import electroblob.wizardry.item.ItemWizardHandbook;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for defining, storing and registering all of wizardry's items. Also registers the ItemBlocks for
 * wizardry's blocks.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryItems {

	private WizardryItems(){} // No instances!

	/** Keeping the material fields in here means {@code @ObjectHolder} ignores them. In actual fact, I could have just
	 * made them private since wizardry only uses them within this class, but in case someone needs them elsewhere I've
	 * used this trick instead to keep them public. */
	public static final class Materials {

        public static final net.minecraft.world.item.Tier MAGICAL = new net.minecraft.world.item.Tier() {
            @Override
            public int getUses() {
                return 100;
            }

            @Override
            public float getSpeed() {
                return 8.0F;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }

            @Override
            public int getLevel() {
                return 3;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public float getAttackDamageBonus() {
                return 4.0F;
            }
        };

        public static enum WizardryArmorMaterial implements ArmorMaterial {
            SILK("silk", 15, new int[]{2, 4, 5, 2}, 15, () -> WizardrySounds.ITEM_ARMOUR_EQUIP_SILK, 0.0F),
            SAGE("sage", 15, new int[]{2, 5, 6, 3}, 25, () -> WizardrySounds.ITEM_ARMOUR_EQUIP_SAGE, 0.0F),
            BATTLEMAGE("battlemage", 15, new int[]{3, 6, 8, 3}, 15, () -> WizardrySounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE, 1.0F),
            WARLOCK("warlock", 20, new int[]{2, 4, 5, 2}, 15, () -> WizardrySounds.ITEM_ARMOUR_EQUIP_WARLOCK, 0.0F);

            private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
            private final String name;
            private final int durabilityMultiplier;
            private final int[] slotProtections;
            private final int enchantmentValue;
            private final Supplier<SoundEvent> sound;
            private final float toughness;

            private WizardryArmorMaterial(String name, int durabilityMultiplier, int[] slotProtections, int enchantmentValue, Supplier<SoundEvent> sound, float toughness) {
                this.name = name;
                this.durabilityMultiplier = durabilityMultiplier;
                this.slotProtections = slotProtections;
                this.enchantmentValue = enchantmentValue;
                this.sound = sound;
                this.toughness = toughness;
            }

            @Override
            public int getDurabilityForSlot(EquipmentSlot p_40410_) {
                return HEALTH_PER_SLOT[p_40410_.getIndex()] * this.durabilityMultiplier;
            }

            @Override
            public int getDefenseForSlot(EquipmentSlot p_40411_) {
                return this.slotProtections[p_40411_.getIndex()];
            }

            @Override
            public int getEnchantmentValue() {
                return this.enchantmentValue;
            }

            @Override
            public SoundEvent getEquipSound() {
                return this.sound.get();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }

            @Override
            public String getName() {
                return this.name;
            }

            @Override
            public float getToughness() {
                return this.toughness;
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        }

	}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Wizardry.MODID);
	public static final DeferredRegister<BannerPattern> PATTERNS = DeferredRegister.create(Registry.BANNER_PATTERN_REGISTRY, Wizardry.MODID);
	
	public static final RegistryObject<BannerPattern> FIRE_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_fire", () -> new BannerPattern("wizardry_fire"));
	public static final RegistryObject<BannerPattern> ICE_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_ice", () -> new BannerPattern("wizardry_ice"));
	public static final RegistryObject<BannerPattern> LIGHTNING_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_lightning", () -> new BannerPattern("wizardry_lightning"));
	public static final RegistryObject<BannerPattern> NECROMANCY_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_necromancy", () -> new BannerPattern("wizardry_necromancy"));
	public static final RegistryObject<BannerPattern> EARTH_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_earth", () -> new BannerPattern("wizardry_earth"));
	public static final RegistryObject<BannerPattern> SORCERY_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_sorcery", () -> new BannerPattern("wizardry_sorcery"));
	public static final RegistryObject<BannerPattern> HEALING_ELEMENTAL_PATTERN = PATTERNS.register("wizardry_healing", () -> new BannerPattern("wizardry_healing"));

    public static final TagKey<BannerPattern> FIRE_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_fire"));
    public static final TagKey<BannerPattern> ICE_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_ice"));
    public static final TagKey<BannerPattern> LIGHTNING_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_lightning"));
    public static final TagKey<BannerPattern> NECROMANCY_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_necromancy"));
    public static final TagKey<BannerPattern> EARTH_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_earth"));
    public static final TagKey<BannerPattern> SORCERY_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_sorcery"));
    public static final TagKey<BannerPattern> HEALING_ELEMENTAL_BANNER = TagKey.create(Registry.BANNER_PATTERN_REGISTRY, new ResourceLocation(Wizardry.MODID, "pattern_item/wizardry_healing"));

	// This is the most concise way I can think of to register the items. Really, I'd prefer it if there was only one
	// point where all the items were listed, but that's not possible within the current system unless you use an array,
	// which means you lose the individual fields...
    
    public static final RegistryObject<Item> MAGIC_CRYSTAL_BLOCK = ITEMS.register("magic_crystal_block", () -> new BlockItem(WizardryBlocks.MAGIC_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> FIRE_CRYSTAL_BLOCK = ITEMS.register("fire_crystal_block", () -> new BlockItem(WizardryBlocks.FIRE_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> ICE_CRYSTAL_BLOCK = ITEMS.register("ice_crystal_block", () -> new BlockItem(WizardryBlocks.ICE_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> LIGHTNING_CRYSTAL_BLOCK = ITEMS.register("lightning_crystal_block", () -> new BlockItem(WizardryBlocks.LIGHTNING_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> NECROMANCY_CRYSTAL_BLOCK = ITEMS.register("necromancy_crystal_block", () -> new BlockItem(WizardryBlocks.NECROMANCY_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> EARTH_CRYSTAL_BLOCK = ITEMS.register("earth_crystal_block", () -> new BlockItem(WizardryBlocks.EARTH_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> SORCERY_CRYSTAL_BLOCK = ITEMS.register("sorcery_crystal_block", () -> new BlockItem(WizardryBlocks.SORCERY_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> HEALING_CRYSTAL_BLOCK = ITEMS.register("healing_crystal_block", () -> new BlockItem(WizardryBlocks.HEALING_CRYSTAL_BLOCK.get(), new Item.Properties().tab(WizardryTabs.WIZARDRY)));

    public static final RegistryObject<Item> MAGIC_CRYSTAL = ITEMS.register("magic_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> FIRE_CRYSTAL = ITEMS.register("fire_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> ICE_CRYSTAL = ITEMS.register("ice_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> LIGHTNING_CRYSTAL = ITEMS.register("lightning_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> NECROMANCY_CRYSTAL = ITEMS.register("necromancy_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> EARTH_CRYSTAL = ITEMS.register("earth_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> SORCERY_CRYSTAL = ITEMS.register("sorcery_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> HEALING_CRSYTAL = ITEMS.register("healing_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));

    public static final RegistryObject<Item> CRYSTAL_SHARD = ITEMS.register("crystal_shard", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));
    public static final RegistryObject<Item> GRAND_CRYSTAL = ITEMS.register("grand_crystal", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));

    public static final RegistryObject<Item> WIZARD_HAND_BOOK = ITEMS.register("wizard_handbook", () -> new ItemWizardHandbook());
    public static final RegistryObject<Item> ARCANE_TOME = ITEMS.register("arcane_tome", () -> new ItemArcaneTome());
    public static final RegistryObject<Item> SPELL_BOOK = ITEMS.register("spell_book", () -> new ItemSpellBook());
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll", () -> new ItemScroll());
    public static final RegistryObject<Item> RUINED_SPELL_BOOK = ITEMS.register("ruined_spell_book", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY).stacksTo(16)));

    public static final RegistryObject<Item> MAGIC_WAND = ITEMS.register("magic_wand", () -> new ItemWand(Tier.NOVICE, null));
    public static final RegistryObject<Item> APPRENTICE_WAND = ITEMS.register("apprentice_wand", () -> new ItemWand(Tier.APPRENTICE, null));
    public static final RegistryObject<Item> ADVANCED_WAND = ITEMS.register("advanced_wand", () -> new ItemWand(Tier.ADVANCED, null));
    public static final RegistryObject<Item> MASTER_WAND = ITEMS.register("master_wand", () -> new ItemWand(Tier.MASTER, null));

    public static final RegistryObject<Item> NOVICE_FIRE_WAND = ITEMS.register("novice_fire_wand", () -> new ItemWand(Tier.NOVICE, Element.FIRE));
    public static final RegistryObject<Item> APPRENTICE_FIRE_WAND = ITEMS.register("apprentice_fire_wand", () -> new ItemWand(Tier.APPRENTICE, Element.FIRE));
    public static final RegistryObject<Item> ADVANCED_FIRE_WAND = ITEMS.register("advanced_fire_wand", () -> new ItemWand(Tier.ADVANCED, Element.FIRE));
    public static final RegistryObject<Item> MASTER_FIRE_WAND = ITEMS.register("master_fire_wand", () -> new ItemWand(Tier.MASTER, Element.FIRE));

    public static final RegistryObject<Item> NOVICE_ICE_WAND = ITEMS.register("novice_ice_wand", () -> new ItemWand(Tier.NOVICE, Element.ICE));
    public static final RegistryObject<Item> APPRENTICE_ICE_WAND = ITEMS.register("apprentice_ice_wand", () -> new ItemWand(Tier.APPRENTICE, Element.ICE));
    public static final RegistryObject<Item> ADVANCED_ICE_WAND = ITEMS.register("advanced_ice_wand", () -> new ItemWand(Tier.ADVANCED, Element.ICE));
    public static final RegistryObject<Item> MASTER_ICE_WAND = ITEMS.register("master_ice_wand", () -> new ItemWand(Tier.MASTER, Element.ICE));

    public static final RegistryObject<Item> NOVICE_LIGHTNING_WAND = ITEMS.register("novice_lightning_wand", () -> new ItemWand(Tier.NOVICE, Element.LIGHTNING));
    public static final RegistryObject<Item> APPRENTICE_LIGHTNING_WAND = ITEMS.register("apprentice_lightning_wand", () -> new ItemWand(Tier.APPRENTICE, Element.LIGHTNING));
    public static final RegistryObject<Item> ADVANCED_LIGHTNING_WAND = ITEMS.register("advanced_lightning_wand", () -> new ItemWand(Tier.ADVANCED, Element.LIGHTNING));
    public static final RegistryObject<Item> MASTER_LIGHTNING_WAND = ITEMS.register("master_lightning_wand", () -> new ItemWand(Tier.MASTER, Element.LIGHTNING));

    public static final RegistryObject<Item> NOVICE_NECROMANCY_WAND = ITEMS.register("novice_necromancy_wand", () -> new ItemWand(Tier.NOVICE, Element.NECROMANCY));
    public static final RegistryObject<Item> APPRENTICE_NECROMANCY_WAND = ITEMS.register("apprentice_necromancy_wand", () -> new ItemWand(Tier.APPRENTICE, Element.NECROMANCY));
    public static final RegistryObject<Item> ADVANCED_NECROMANCY_WAND = ITEMS.register("advanced_necromancy_wand", () -> new ItemWand(Tier.ADVANCED, Element.NECROMANCY));
    public static final RegistryObject<Item> MASTER_NECROMANCY_WAND = ITEMS.register("master_necromancy_wand", () -> new ItemWand(Tier.MASTER, Element.NECROMANCY));

    public static final RegistryObject<Item> NOVICE_EARTH_WAND = ITEMS.register("novice_earth_wand", () -> new ItemWand(Tier.NOVICE, Element.EARTH));
    public static final RegistryObject<Item> APPRENTICE_EARTH_WAND = ITEMS.register("apprentice_earth_wand", () -> new ItemWand(Tier.APPRENTICE, Element.EARTH));
    public static final RegistryObject<Item> ADVANCED_EARTH_WAND = ITEMS.register("advanced_earth_wand", () -> new ItemWand(Tier.ADVANCED, Element.EARTH));
    public static final RegistryObject<Item> MASTER_EARTH_WAND = ITEMS.register("master_earth_wand", () -> new ItemWand(Tier.MASTER, Element.EARTH));

    public static final RegistryObject<Item> NOVICE_SORCERY_WAND = ITEMS.register("novice_sorcery_wand", () -> new ItemWand(Tier.NOVICE, Element.SORCERY));
    public static final RegistryObject<Item> APPRENTICE_SORCERY_WAND = ITEMS.register("apprentice_sorcery_wand", () -> new ItemWand(Tier.APPRENTICE, Element.SORCERY));
    public static final RegistryObject<Item> ADVANCED_SORCERY_WAND = ITEMS.register("advanced_sorcery_wand", () -> new ItemWand(Tier.ADVANCED, Element.SORCERY));
    public static final RegistryObject<Item> MASTER_SORCERY_WAND = ITEMS.register("master_sorcery_wand", () -> new ItemWand(Tier.MASTER, Element.SORCERY));

    public static final RegistryObject<Item> NOVICE_HEALING_WAND = ITEMS.register("novice_healing_wand", () -> new ItemWand(Tier.NOVICE, Element.HEALING));
    public static final RegistryObject<Item> APPRENTICE_HEALING_WAND = ITEMS.register("apprentice_healing_wand", () -> new ItemWand(Tier.APPRENTICE, Element.HEALING));
    public static final RegistryObject<Item> ADVANCED_HEALING_WAND = ITEMS.register("advanced_healing_wand", () -> new ItemWand(Tier.ADVANCED, Element.HEALING));
    public static final RegistryObject<Item> MASTER_HEALING_WAND = ITEMS.register("master_healing_wand", () -> new ItemWand(Tier.MASTER, Element.HEALING));

    public static final RegistryObject<Item> SPECTRAL_SWORD = ITEMS.register("spectral_sword", () -> new ItemSpectralSword(Tiers.IRON, 3, -2.4F));
    public static final RegistryObject<Item> SPECTRAL_PICKAXE = ITEMS.register("spectral_pickaxe", () -> new ItemSpectralPickaxe(Tiers.IRON, 1, -2.8F));
    public static final RegistryObject<Item> SPECTRAL_BOW = ITEMS.register("spectral_bow", () -> new ItemSpectralBow());

    public static final RegistryObject<Item> BLANK_SCROLL = ITEMS.register("blank_scroll", () -> new ItemBlankScroll());
    public static final RegistryObject<Item> MAGIC_SILK = ITEMS.register("magic_silk", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)));

    public static final RegistryObject<Item> SMALL_MANA_FLASK = ITEMS.register("small_mana_flask", () -> new ItemManaFlask(ItemManaFlask.Size.SMALL));
    public static final RegistryObject<Item> MEDIUM_MANA_FLASK = ITEMS.register("medium_mana_flask", () -> new ItemManaFlask(ItemManaFlask.Size.MEDIUM));
    public static final RegistryObject<Item> LARGE_MANA_FLASK = ITEMS.register("large_mana_flask", () -> new ItemManaFlask(ItemManaFlask.Size.LARGE));

    public static final RegistryObject<Item> STORAGE_UPGRADE = ITEMS.register("storage_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> SIPHON_UPGRADE = ITEMS.register("siphon_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> CONDENSER_UPGRADE = ITEMS.register("condenser_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> RANGE_UPGRADE = ITEMS.register("range_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> DURATION_UPGRADE = ITEMS.register("duration_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> COOLDOWN_UPGRADE = ITEMS.register("cooldown_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> BLAST_UPGRADE = ITEMS.register("blast_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> ATTUNEMENT_UPGRADE = ITEMS.register("attunement_upgrade", () -> new ItemWandUpgrade());
    public static final RegistryObject<Item> MELEE_UPGRADE = ITEMS.register("melee_upgrade", () -> new ItemWandUpgrade());

    public static final RegistryObject<Item> FLAMING_AXE = ITEMS.register("flaming_axe", () -> new ItemFlamingAxe(Materials.MAGICAL));
    public static final RegistryObject<Item> FROST_AXE = ITEMS.register("frost_axe", () -> new ItemFrostAxe(Materials.MAGICAL));

    public static final RegistryObject<Item> IDENTIFICATION_SCROLL = ITEMS.register("identification_scroll", () -> new ItemIdentificationScroll());

    public static final RegistryObject<Item> RESPLENDENT_THREAD = ITEMS.register("resplendent_thread", () -> new ItemArmourUpgrade());
    public static final RegistryObject<Item> CRYSTAL_SILVER_PLATING = ITEMS.register("crystal_silver_plating", () -> new ItemArmourUpgrade());
    public static final RegistryObject<Item> ETHEREAL_CRYSTALWEAVE = ITEMS.register("ethereal_crystalweave", () -> new ItemArmourUpgrade());
    public static final RegistryObject<Item> ASTRAL_DIAMOND = ITEMS.register("astral_diamond", () -> new Item(new Item.Properties().tab(WizardryTabs.WIZARDRY)) {
        @Override
        public Rarity getRarity(ItemStack stack) {
            return Rarity.RARE;
        }
    });
    public static final RegistryObject<Item> PURIFYING_ELIXIR = ITEMS.register("purifying_elixir", () -> new ItemPurifyingElixir());

    public static final RegistryObject<Item> FIREBOMB = ITEMS.register("firebomb", () -> new ItemFirebomb());
    public static final RegistryObject<Item> POISON_BOMB = ITEMS.register("poison_bomb", () -> new ItemPoisonBomb());
    public static final RegistryObject<Item> SMOKE_BOMB = ITEMS.register("smoke_bomb", () -> new ItemSmokeBomb());
    public static final RegistryObject<Item> SPARK_BOMB = ITEMS.register("spark_bomb", () -> new ItemSparkBomb());

    public static final RegistryObject<Item> FIRE_SPECTRAL_DUST = ITEMS.register("fire_spectral_dust", () -> new ItemSpectralDust(Element.FIRE));
    public static final RegistryObject<Item> ICE_SPECTRAL_DUST = ITEMS.register("ice_spectral_dust", () -> new ItemSpectralDust(Element.ICE));
    public static final RegistryObject<Item> LIGHTNING_SPECTRAL_DUST = ITEMS.register("lightning_spectral_dust", () -> new ItemSpectralDust(Element.LIGHTNING));
    public static final RegistryObject<Item> NECROMANCY_SPECTRAL_DUST = ITEMS.register("necromancy_spectral_dust", () -> new ItemSpectralDust(Element.NECROMANCY));
    public static final RegistryObject<Item> EARTH_SPECTRAL_DUST = ITEMS.register("earth_spectral_dust", () -> new ItemSpectralDust(Element.EARTH));
    public static final RegistryObject<Item> SORCERY_SPECTRAL_DUST = ITEMS.register("sorcery_spectral_dust", () -> new ItemSpectralDust(Element.SORCERY));
    public static final RegistryObject<Item> HEALING_SPECTRAL_DUST = ITEMS.register("healing_spectral_dust", () -> new ItemSpectralDust(Element.HEALING));

    public static final RegistryObject<Item> WIZARD_HAT = ITEMS.register("wizard_hat", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, null));
    public static final RegistryObject<Item> WIZARD_ROBE = ITEMS.register("wizard_robe", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, null));
    public static final RegistryObject<Item> WIZARD_LEGGINGS = ITEMS.register("wizard_leggings", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, null));
    public static final RegistryObject<Item> WIZARD_BOOTS = ITEMS.register("wizard_boots", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, null));

    public static final RegistryObject<Item> WIZARD_HAT_FIRE = ITEMS.register("wizard_hat_fire", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.FIRE));
    public static final RegistryObject<Item> WIZARD_ROBE_FIRE = ITEMS.register("wizard_robe_fire", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.FIRE));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_FIRE = ITEMS.register("wizard_leggings_fire", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.FIRE));
    public static final RegistryObject<Item> WIZARD_BOOTS_FIRE = ITEMS.register("wizard_boots_fire", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.FIRE));

    public static final RegistryObject<Item> WIZARD_HAT_ICE = ITEMS.register("wizard_hat_ice", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.ICE));
    public static final RegistryObject<Item> WIZARD_ROBE_ICE = ITEMS.register("wizard_robe_ice", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.ICE));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_ICE = ITEMS.register("wizard_leggings_ice", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.ICE));
    public static final RegistryObject<Item> WIZARD_BOOTS_ICE = ITEMS.register("wizard_boots_ice", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.ICE));

    public static final RegistryObject<Item> WIZARD_HAT_LIGHTNING = ITEMS.register("wizard_hat_lightning", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.LIGHTNING));
    public static final RegistryObject<Item> WIZARD_ROBE_LIGHTNING = ITEMS.register("wizard_robe_lightning", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.LIGHTNING));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_LIGHTNING = ITEMS.register("wizard_leggings_lightning", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.LIGHTNING));
    public static final RegistryObject<Item> WIZARD_BOOTS_LIGHTNING = ITEMS.register("wizard_boots_lightning", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.LIGHTNING));

    public static final RegistryObject<Item> WIZARD_HAT_NECROMANCY = ITEMS.register("wizard_hat_necromancy", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.NECROMANCY));
    public static final RegistryObject<Item> WIZARD_ROBE_NECROMANCY = ITEMS.register("wizard_robe_necromancy", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.NECROMANCY));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_NECROMANCY = ITEMS.register("wizard_leggings_necromancy", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.NECROMANCY));
    public static final RegistryObject<Item> WIZARD_BOOTS_NECROMANCY = ITEMS.register("wizard_boots_necromancy", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.NECROMANCY));

    public static final RegistryObject<Item> WIZARD_HAT_EARTH = ITEMS.register("wizard_hat_earth", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.EARTH));
    public static final RegistryObject<Item> WIZARD_ROBE_EARTH = ITEMS.register("wizard_robe_earth", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.EARTH));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_EARTH = ITEMS.register("wizard_leggings_earth", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.EARTH));
    public static final RegistryObject<Item> WIZARD_BOOTS_EARTH = ITEMS.register("wizard_boots_earth", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.EARTH));

    public static final RegistryObject<Item> WIZARD_HAT_SORCERY = ITEMS.register("wizard_hat_sorcery", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.SORCERY));
    public static final RegistryObject<Item> WIZARD_ROBE_SORCERY = ITEMS.register("wizard_robe_sorcery", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.SORCERY));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_SORCERY = ITEMS.register("wizard_leggings_sorcery", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.SORCERY));
    public static final RegistryObject<Item> WIZARD_BOOTS_SORCERY = ITEMS.register("wizard_boots_sorcery", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.SORCERY));

    public static final RegistryObject<Item> WIZARD_HAT_HEALING = ITEMS.register("wizard_hat_healing", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.HEAD, Element.HEALING));
    public static final RegistryObject<Item> WIZARD_ROBE_HEALING = ITEMS.register("wizard_robe_healing", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.CHEST, Element.HEALING));
    public static final RegistryObject<Item> WIZARD_LEGGINGS_HEALING = ITEMS.register("wizard_leggings_healing", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.LEGS, Element.HEALING));
    public static final RegistryObject<Item> WIZARD_BOOTS_HEALING = ITEMS.register("wizard_boots_healing", () -> new ItemWizardArmour(ArmourClass.WIZARD, EquipmentSlot.FEET, Element.HEALING));

    public static final RegistryObject<Item> SAGE_HAT = ITEMS.register("sage_hat", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, null));
    public static final RegistryObject<Item> SAGE_ROBE = ITEMS.register("sage_robe", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, null));
    public static final RegistryObject<Item> SAGE_LEGGINGS = ITEMS.register("sage_leggings", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, null));
    public static final RegistryObject<Item> SAGE_BOOTS = ITEMS.register("sage_boots", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, null));

    public static final RegistryObject<Item> SAGE_HAT_FIRE = ITEMS.register("sage_hat_fire", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.FIRE));
    public static final RegistryObject<Item> SAGE_ROBE_FIRE = ITEMS.register("sage_robe_fire", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.FIRE));
    public static final RegistryObject<Item> SAGE_LEGGINGS_FIRE = ITEMS.register("sage_leggings_fire", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.FIRE));
    public static final RegistryObject<Item> SAGE_BOOTS_FIRE = ITEMS.register("sage_boots_fire", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.FIRE));

    public static final RegistryObject<Item> SAGE_HAT_ICE = ITEMS.register("sage_hat_ice", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.ICE));
    public static final RegistryObject<Item> SAGE_ROBE_ICE = ITEMS.register("sage_robe_ice", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.ICE));
    public static final RegistryObject<Item> SAGE_LEGGINGS_ICE = ITEMS.register("sage_leggings_ice", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.ICE));
    public static final RegistryObject<Item> SAGE_BOOTS_ICE = ITEMS.register("sage_boots_ice", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.ICE));

    public static final RegistryObject<Item> SAGE_HAT_LIGHTNING = ITEMS.register("sage_hat_lightning", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.LIGHTNING));
    public static final RegistryObject<Item> SAGE_ROBE_LIGHTNING = ITEMS.register("sage_robe_lightning", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.LIGHTNING));
    public static final RegistryObject<Item> SAGE_LEGGINGS_LIGHTNING = ITEMS.register("sage_leggings_lightning", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.LIGHTNING));
    public static final RegistryObject<Item> SAGE_BOOTS_LIGHTNING = ITEMS.register("sage_boots_lightning", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.LIGHTNING));

    public static final RegistryObject<Item> SAGE_HAT_NECROMANCY = ITEMS.register("sage_hat_necromancy", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.NECROMANCY));
    public static final RegistryObject<Item> SAGE_ROBE_NECROMANCY = ITEMS.register("sage_robe_necromancy", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.NECROMANCY));
    public static final RegistryObject<Item> SAGE_LEGGINGS_NECROMANCY = ITEMS.register("sage_leggings_necromancy", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.NECROMANCY));
    public static final RegistryObject<Item> SAGE_BOOTS_NECROMANCY = ITEMS.register("sage_boots_necromancy", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.NECROMANCY));

    public static final RegistryObject<Item> SAGE_HAT_EARTH = ITEMS.register("sage_hat_earth", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.EARTH));
    public static final RegistryObject<Item> SAGE_ROBE_EARTH = ITEMS.register("sage_robe_earth", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.EARTH));
    public static final RegistryObject<Item> SAGE_LEGGINGS_EARTH = ITEMS.register("sage_leggings_earth", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.EARTH));
    public static final RegistryObject<Item> SAGE_BOOTS_EARTH = ITEMS.register("sage_boots_earth", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.EARTH));

    public static final RegistryObject<Item> SAGE_HAT_SORCERY = ITEMS.register("sage_hat_sorcery", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.SORCERY));
    public static final RegistryObject<Item> SAGE_ROBE_SORCERY = ITEMS.register("sage_robe_sorcery", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.SORCERY));
    public static final RegistryObject<Item> SAGE_LGGINGS_SORCERY = ITEMS.register("sage_leggings_sorcery", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.SORCERY));
    public static final RegistryObject<Item> SAGE_BOOTS_SORCERY = ITEMS.register("sage_boots_sorcery", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.SORCERY));

    public static final RegistryObject<Item> SAEG_HAT_HEALING = ITEMS.register("sage_hat_healing", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.HEAD, Element.HEALING));
    public static final RegistryObject<Item> SAGE_ROBE_HEALING = ITEMS.register("sage_robe_healing", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.CHEST, Element.HEALING));
    public static final RegistryObject<Item> SAGE_LEGGINGS_HEALING = ITEMS.register("sage_leggings_healing", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.LEGS, Element.HEALING));
    public static final RegistryObject<Item> SAGE_BOOTS_HEALING = ITEMS.register("sage_boots_healing", () -> new ItemWizardArmour(ArmourClass.SAGE, EquipmentSlot.FEET, Element.HEALING));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET = ITEMS.register("battlemage_helmet", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, null));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE = ITEMS.register("battlemage_chestplate", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, null));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS = ITEMS.register("battlemage_leggings", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, null));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS = ITEMS.register("battlemage_boots", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, null));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_FIRE = ITEMS.register("battlemage_helmet_fire", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.FIRE));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_FIRE = ITEMS.register("battlemage_chestplate_fire", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.FIRE));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_FIRE = ITEMS.register("battlemage_leggings_fire", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.FIRE));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_FIRE = ITEMS.register("battlemage_boots_fire", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.FIRE));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_ICE = ITEMS.register("battlemage_helmet_ice", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.ICE));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_ICE = ITEMS.register("battlemage_chestplate_ice", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.ICE));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_ICE = ITEMS.register("battlemage_leggings_ice", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.ICE));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_ICE = ITEMS.register("battlemage_boots_ice", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.ICE));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_LIGHTNING = ITEMS.register("battlemage_helmet_lightning", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.LIGHTNING));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_LIGHTNING = ITEMS.register("battlemage_chestplate_lightning", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.LIGHTNING));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_LIGHTNING = ITEMS.register("battlemage_leggings_lightning", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.LIGHTNING));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_LIGHTNING = ITEMS.register("battlemage_boots_lightning", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.LIGHTNING));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_NECROMANCY = ITEMS.register("battlemage_helmet_necromancy", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.NECROMANCY));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_NECROMANCY = ITEMS.register("battlemage_chestplate_necromancy", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.NECROMANCY));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_NECROMANCY = ITEMS.register("battlemage_leggings_necromancy", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.NECROMANCY));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_NECROMANCY = ITEMS.register("battlemage_boots_necromancy", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.NECROMANCY));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_EARTH = ITEMS.register("battlemage_helmet_earth", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.EARTH));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_EARTH = ITEMS.register("battlemage_chestplate_earth", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.EARTH));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_EARTH = ITEMS.register("battlemage_leggings_earth", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.EARTH));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_EARTH = ITEMS.register("battlemage_boots_earth", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.EARTH));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_SORCERY = ITEMS.register("battlemage_helmet_sorcery", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.SORCERY));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_SORCERY = ITEMS.register("battlemage_chestplate_sorcery", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.SORCERY));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_SORCERY = ITEMS.register("battlemage_leggings_sorcery", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.SORCERY));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_SORCERY = ITEMS.register("battlemage_boots_sorcery", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.SORCERY));

    public static final RegistryObject<Item> BATTLEMAGE_HELMET_HEALING = ITEMS.register("battlemage_helmet_healing", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.HEAD, Element.HEALING));
    public static final RegistryObject<Item> BATTLEMAGE_CHESTPLATE_HEALING = ITEMS.register("battlemage_chestplate_healing", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.CHEST, Element.HEALING));
    public static final RegistryObject<Item> BATTLEMAGE_LEGGINGS_HEALING = ITEMS.register("battlemage_leggings_healing", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.LEGS, Element.HEALING));
    public static final RegistryObject<Item> BATTLEMAGE_BOOTS_HEALING = ITEMS.register("battlemage_boots_healing", () -> new ItemWizardArmour(ArmourClass.BATTLEMAGE, EquipmentSlot.FEET, Element.HEALING));

    public static final RegistryObject<Item> WARLOCK_HOOD = ITEMS.register("warlock_hood", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, null));
    public static final RegistryObject<Item> WARLOCK_ROBE = ITEMS.register("warlock_robe", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, null));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS = ITEMS.register("warlock_leggings", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, null));
    public static final RegistryObject<Item> WARLOCK_BOOTS = ITEMS.register("warlock_boots", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, null));

    public static final RegistryObject<Item> WARLOCK_HOOD_FIRE = ITEMS.register("warlock_hood_fire", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.FIRE));
    public static final RegistryObject<Item> WARLOCK_ROBE_FIRE = ITEMS.register("warlock_robe_fire", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.FIRE));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_FIRE = ITEMS.register("warlock_leggings_fire", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.FIRE));
    public static final RegistryObject<Item> WARLOCK_BOOTS_FIRE = ITEMS.register("warlock_boots_fire", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.FIRE));

    public static final RegistryObject<Item> WARLOCK_HOOD_ICE = ITEMS.register("warlock_hood_ice", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.ICE));
    public static final RegistryObject<Item> WARLOCK_ROBE_ICE = ITEMS.register("warlock_robe_ice", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.ICE));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_ICE = ITEMS.register("warlock_leggings_ice", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.ICE));
    public static final RegistryObject<Item> WARLOCK_BOOTS_ICE = ITEMS.register("warlock_boots_ice", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.ICE));

    public static final RegistryObject<Item> WARLOCK_HOOD_LIGHTNING = ITEMS.register("warlock_hood_lightning", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.LIGHTNING));
    public static final RegistryObject<Item> WARLOCK_ROBE_LIGHTNING = ITEMS.register("warlock_robe_lightning", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.LIGHTNING));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_LIGHTNING = ITEMS.register("warlock_leggings_lightning", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.LIGHTNING));
    public static final RegistryObject<Item> WARLOCK_BOOTS_LIGHTNING = ITEMS.register("warlock_boots_lightning", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.LIGHTNING));

    public static final RegistryObject<Item> WARLOCK_HOOD_NECROMANCY = ITEMS.register("warlock_hood_necromancy", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.NECROMANCY));
    public static final RegistryObject<Item> WARLOCK_ROBE_NECROMANCY = ITEMS.register("warlock_robe_necromancy", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.NECROMANCY));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_NECROMANCY = ITEMS.register("warlock_leggings_necromancy", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.NECROMANCY));
    public static final RegistryObject<Item> WARLOCK_BOOTS_NECROMANCY = ITEMS.register("warlock_boots_necromancy", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.NECROMANCY));

    public static final RegistryObject<Item> WARLOCK_HOOD_EARTH = ITEMS.register("warlock_hood_earth", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.EARTH));
    public static final RegistryObject<Item> WARLOCK_ROBE_EARTH = ITEMS.register("warlock_robe_earth", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.EARTH));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_EARTH = ITEMS.register("warlock_leggings_earth", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.EARTH));
    public static final RegistryObject<Item> WARLOCK_BOOTS_EARTH = ITEMS.register("warlock_boots_earth", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.EARTH));

    public static final RegistryObject<Item> WARLOCK_HOOD_SORCERY = ITEMS.register("warlock_hood_sorcery", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.SORCERY));
    public static final RegistryObject<Item> WARLOCK_ROBE_SORCERY = ITEMS.register("warlock_robe_sorcery", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.SORCERY));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_SORCERY = ITEMS.register("warlock_leggings_sorcery", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.SORCERY));
    public static final RegistryObject<Item> WARLOCK_BOOTS_SORCERY = ITEMS.register("warlock_boots_sorcery", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.SORCERY));

    public static final RegistryObject<Item> WARLOCK_HOOD_HEALING = ITEMS.register("warlock_hood_healing", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.HEAD, Element.HEALING));
    public static final RegistryObject<Item> WARLOCK_ROBE_HEALING = ITEMS.register("warlock_robe_healing", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.CHEST, Element.HEALING));
    public static final RegistryObject<Item> WARLOCK_LEGGINGS_HEALING = ITEMS.register("warlock_leggings_healing", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.LEGS, Element.HEALING));
    public static final RegistryObject<Item> WARLOCK_BOOTS_HEALING = ITEMS.register("warlock_boots_healing", () -> new ItemWizardArmour(ArmourClass.WARLOCK, EquipmentSlot.FEET, Element.HEALING));

    public static final RegistryObject<Item> SPECTRAL_HELMET = ITEMS.register("spectral_helmet", () -> new ItemSpectralArmour(ArmorMaterials.IRON, EquipmentSlot.HEAD));
    public static final RegistryObject<Item> SPECTRAL_CHESTPLATE = ITEMS.register("spectral_chestplate", () -> new ItemSpectralArmour(ArmorMaterials.IRON, EquipmentSlot.CHEST));
    public static final RegistryObject<Item> SPECTRAL_LEGGINGS = ITEMS.register("spectral_leggings", () -> new ItemSpectralArmour(ArmorMaterials.IRON, EquipmentSlot.LEGS));
    public static final RegistryObject<Item> SPECTRAL_BOOTS = ITEMS.register("spectral_boots", () -> new ItemSpectralArmour(ArmorMaterials.IRON, EquipmentSlot.FEET));
    
    public static final RegistryObject<Item> LIGHTNING_HAMMER = ITEMS.register("lightning_hammer", () -> new ItemLightningHammer());
    
    public static final RegistryObject<Item> FLAMECATCHER = ITEMS.register("flamecatcher", () -> new ItemFlamecatcher());

    public static final RegistryObject<Item> RING_CONDENSING = ITEMS.register("ring_condensing", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_SIPHONING = ITEMS.register("ring_siphoning", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_BATTLEMAGE = ITEMS.register("ring_battlemage", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_COMBUSTION = ITEMS.register("ring_combustion", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_FIRE_MELEE = ITEMS.register("ring_fire_melee", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_FIRE_BIOME = ITEMS.register("ring_fire_biome", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_DISINTEGRATION = ITEMS.register("ring_disintegration", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_METEOR = ITEMS.register("ring_meteor", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_ICE_MELEE = ITEMS.register("ring_ice_melee", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_ICE_BIOME = ITEMS.register("ring_ice_biome", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_ARCANE_FROST = ITEMS.register("ring_arcane_frost", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_SHATTERING = ITEMS.register("ring_shattering", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_LIGHTNING_MELEE = ITEMS.register("ring_lightning_melee", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_STORM = ITEMS.register("ring_storm", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_SEEKING = ITEMS.register("ring_seeking", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_HAMMER = ITEMS.register("ring_hammer", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_STORMCLOUD = ITEMS.register("ring_stormcloud", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_SOULBINDING = ITEMS.register("ring_soulbinding", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_LEECHING = ITEMS.register("ring_leeching", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_NECROMANCY_MELEE = ITEMS.register("ring_necromancy_melee", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_MIND_CONTROL = ITEMS.register("ring_mind_control", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_POISON = ITEMS.register("ring_poison", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_EARTH_MELEE = ITEMS.register("ring_earth_melee", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_EARTH_BIOME = ITEMS.register("ring_earth_biome", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_FULL_MOON = ITEMS.register("ring_full_moon", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_EVOKER = ITEMS.register("ring_evoker", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_EXTRACTION = ITEMS.register("ring_extraction", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_MANA_RETURN = ITEMS.register("ring_mana_return", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_BLOCKWRANGLER = ITEMS.register("ring_blockwrangler", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_CONJURER = ITEMS.register("ring_conjurer", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_DEFENDER = ITEMS.register("ring_defender", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_PALADIN = ITEMS.register("ring_paladin", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.RING));
    public static final RegistryObject<Item> RING_INTERDICTION = ITEMS.register("ring_interdiction", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.RING));

    public static final RegistryObject<Item> AMULET_ARCANE_DEFENCE = ITEMS.register("amulet_arcane_defence", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_WARDING = ITEMS.register("amulet_warding", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_WISDOM = ITEMS.register("amulet_wisdom", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_FIRE_PROTECTION = ITEMS.register("amulet_fire_protection", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_FIRE_CLOAKING = ITEMS.register("amulet_fire_cloaking", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_ICE_IMMUNITY = ITEMS.register("amulet_ice_immunity", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_ICE_PROTECTION = ITEMS.register("amulet_ice_protection", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_FROST_WARDING = ITEMS.register("amulet_frost_warding", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_POTENTIAL = ITEMS.register("amulet_potential", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_CHANNELING = ITEMS.register("amulet_channeling", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_LICH = ITEMS.register("amulet_lich", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_WITHER_IMMUNITY = ITEMS.register("amulet_wither_immunity", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_GLIDE = ITEMS.register("amulet_glide", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_BANISHING = ITEMS.register("amulet_banishing", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_ANCHORING = ITEMS.register("amulet_anchoring", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_RECOVERY = ITEMS.register("amulet_recovery", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_TRANSIENCE = ITEMS.register("amulet_transience", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_RESURRECTION = ITEMS.register("amulet_resurrection", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_AUTO_SHIELD = ITEMS.register("amulet_auto_shield", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.NECKLACE));
    public static final RegistryObject<Item> AMULET_ABSORPTION = ITEMS.register("amulet_absorption", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.NECKLACE));

    public static final RegistryObject<Item> CHARM_HAGGLER = ITEMS.register("charm_haggler", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_EXPERIENCE_TOME = ITEMS.register("charm_experience_tome", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_MOVE_SPEED = ITEMS.register("charm_move_speed", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_SPELL_DISCOVERY = ITEMS.register("charm_spell_discovery", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_AUTO_SMELT = ITEMS.register("charm_auto_smelt", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_LAVA_WALKING = ITEMS.register("charm_lava_walking", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_STORM = ITEMS.register("charm_storm", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_MINION_HEALTH = ITEMS.register("charm_minion_health", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_MINION_VARIANTS = ITEMS.register("charm_minion_variants", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_UNDEAD_HELMETS = ITEMS.register("charm_undead_helmets", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_HUNGER_CASTING = ITEMS.register("charm_hunger_casting", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_FLIGHT = ITEMS.register("charm_flight", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_GROWTH = ITEMS.register("charm_growth", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_ABSEILING = ITEMS.register("charm_abseiling", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_SILK_TOUCH = ITEMS.register("charm_silk_touch", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_SIXTH_SENSE = ITEMS.register("charm_sixth_sense", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_STOP_TIME = ITEMS.register("charm_stop_time", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_LIGHT = ITEMS.register("charm_light", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_TRANSPORTATION = ITEMS.register("charm_transportation", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_BLACK_HOLE = ITEMS.register("charm_black_hole", () -> new ItemArtefact(Rarity.EPIC, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_MOUNT_TELEPORTING = ITEMS.register("charm_mount_teleporting", () -> new ItemArtefact(Rarity.RARE, ItemArtefact.Type.CHARM));
    public static final RegistryObject<Item> CHARM_FEEDING = ITEMS.register("charm_feeding", () -> new ItemArtefact(Rarity.UNCOMMON, ItemArtefact.Type.CHARM));
}