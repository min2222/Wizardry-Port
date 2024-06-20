package electroblob.wizardry.registry;

import javax.annotation.Nonnull;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityCombustionRune;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityFireSigil;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySpectralGolem;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityVexMinion;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import electroblob.wizardry.entity.projectile.EntityDart;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityForceOrb;
import electroblob.wizardry.entity.projectile.EntityIceCharge;
import electroblob.wizardry.entity.projectile.EntityIceLance;
import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.entity.projectile.EntityIceball;
import electroblob.wizardry.entity.projectile.EntityLargeMagicFireball;
import electroblob.wizardry.entity.projectile.EntityLightningArrow;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import electroblob.wizardry.entity.projectile.EntityMagicFireball;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.entity.projectile.EntitySpark;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.spell.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

/**
 * Class responsible for defining, storing and registering all of wizardry's spells. Use this to access individual
 * spell instances, similar to the {@code Blocks} and {@code Items} classes.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class Spells {

	private Spells(){} // No instances!

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }

	// Wizardry 1.0 spells

    public static final Spell NONE = new None();
    public static final Spell MAGIC_MISSILE = new SpellArrow<EntityMagicMissile>("magic_missile", EntityMagicMissile::new) {
        @Override
        protected String getTranslationKey() {
            return Wizardry.tisTheSeason ? super.getTranslationKey() + "_festive" : super.getTranslationKey();
        }
    }.addProperties(Spell.DAMAGE).soundValues(1, 1.4f, 0.4f);
    public static final Spell IGNITE = new Ignite();
    public static final Spell FREEZE = new Freeze();
    public static final Spell SNOWBALL = new SpellThrowable<>("snowball", Snowball::new).npcSelector((e, o) -> o).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell ARC = new Arc();
    public static final Spell THUNDERBOLT = new SpellProjectile<>("thunderbolt", EntityThunderbolt::new).addProperties(Spell.DAMAGE, EntityThunderbolt.KNOCKBACK_STRENGTH).soundValues(0.8f, 0.9f, 0.2f);
    public static final Spell SUMMON_ZOMBIE = new SummonZombie();
    public static final Spell SNARE = new Snare();
    public static final Spell DART = new SpellArrow<>("dart", EntityDart::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell LIGHT = new Light();
    public static final Spell TELEKINESIS = new Telekinesis();
    public static final Spell HEAL = new Heal();

    public static final Spell FIREBALL = new SpellProjectile<>("fireball", EntityMagicFireball::new).addProperties(Spell.DAMAGE, Spell.BURN_DURATION);
    public static final Spell FLAME_RAY = new FlameRay();
    public static final Spell FIREBOMB = new SpellProjectile<>("firebomb", EntityFirebomb::new).addProperties(Spell.DIRECT_DAMAGE, Spell.SPLASH_DAMAGE, Spell.BLAST_RADIUS, Spell.BURN_DURATION).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell FIRE_SIGIL = new SpellConstructRanged<>("fire_sigil", EntityFireSigil::new, true).floor(true).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.BURN_DURATION);
    public static final Spell FIREBOLT = new SpellProjectile<>("firebolt", EntityFirebolt::new).addProperties(Spell.DAMAGE, Spell.BURN_DURATION);
    public static final Spell FROST_RAY = new FrostRay();
    public static final Spell SUMMON_SNOW_GOLEM = new SummonSnowGolem();
    public static final Spell ICE_SHARD = new SpellArrow<>("ice_shard", EntityIceShard::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(1, 1.6f, 0.4f);
    public static final Spell ICE_STATUE = new IceStatue();
    public static final Spell FROST_SIGIL = new SpellConstructRanged<>("frost_sigil", EntityFrostSigil::new, true).floor(true).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH);
    public static final Spell LIGHTNING_RAY = new LightningRay();
    public static final Spell SPARK_BOMB = new SpellProjectile<>("spark_bomb", EntitySparkBomb::new).addProperties(Spell.DIRECT_DAMAGE, Spell.EFFECT_RADIUS, EntitySparkBomb.SECONDARY_MAX_TARGETS, Spell.SPLASH_DAMAGE).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell HOMING_SPARK = new SpellProjectile<>("homing_spark", EntitySpark::new).addProperties(Spell.DAMAGE, Spell.SEEKING_STRENGTH).soundValues(1.0f, 0.4f, 0.2f);
    public static final Spell LIGHTNING_SIGIL = new SpellConstructRanged<>("lightning_sigil", EntityLightningSigil::new, true).floor(true).addProperties(Spell.EFFECT_RADIUS, Spell.DIRECT_DAMAGE, Spell.EFFECT_RADIUS, EntityLightningSigil.SECONDARY_RANGE, EntityLightningSigil.SECONDARY_MAX_TARGETS, Spell.SPLASH_DAMAGE);
    public static final Spell LIGHTNING_ARROW = new SpellArrow<>("lightning_arrow", EntityLightningArrow::new).addProperties(Spell.DAMAGE).soundValues(1, 1.45f, 0.3f);
    public static final Spell LIFE_DRAIN = new LifeDrain();
    public static final Spell SUMMON_SKELETON = new SummonSkeleton();
    public static final Spell METAMORPHOSIS = new Metamorphosis();
    public static final Spell WITHER = new Wither();
    public static final Spell POISON = new Poison();
    public static final Spell GROWTH_AURA = new GrowthAura();
    public static final Spell BUBBLE = new Bubble();
    public static final Spell WHIRLWIND = new Whirlwind();
    public static final Spell POISON_BOMB = new SpellProjectile<>("poison_bomb", EntityPoisonBomb::new).addProperties(Spell.DIRECT_DAMAGE, Spell.EFFECT_RADIUS, Spell.DIRECT_EFFECT_DURATION, Spell.DIRECT_EFFECT_STRENGTH, Spell.SPLASH_DAMAGE, Spell.SPLASH_EFFECT_DURATION, Spell.SPLASH_EFFECT_STRENGTH).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell SUMMON_SPIRIT_WOLF = new SummonSpiritWolf();
    public static final Spell BLINK = new Blink();
    public static final Spell AGILITY = new SpellBuff("agility", 0.4f, 1.0f, 0.8f, () -> MobEffects.MOVEMENT_SPEED, () -> MobEffects.JUMP).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell CONJURE_SWORD = new SpellConjuration("conjure_sword", () -> WizardryItems.SPECTRAL_SWORD.get());
    public static final Spell CONJURE_PICKAXE = new SpellConjuration("conjure_pickaxe", () -> WizardryItems.SPECTRAL_PICKAXE.get());
    public static final Spell CONJURE_BOW = new SpellConjuration("conjure_bow", () -> WizardryItems.SPECTRAL_BOW.get());
    public static final Spell FORCE_ARROW = new ForceArrow();
    public static final Spell SHIELD = new Shield();
    public static final Spell REPLENISH_HUNGER = new ReplenishHunger();
    public static final Spell CURE_EFFECTS = new CureEffects();
    public static final Spell HEAL_ALLY = new HealAlly();

	public static final Spell summon_blaze = placeholder();
	public static final Spell ring_of_fire = placeholder();
	public static final Spell detonate = placeholder();
	public static final Spell fire_resistance = placeholder();
	public static final Spell fireskin = placeholder();
	public static final Spell flaming_axe = placeholder();
	public static final Spell blizzard = placeholder();
	public static final Spell summon_ice_wraith = placeholder();
	public static final Spell ice_shroud = placeholder();
	public static final Spell ice_charge = placeholder();
	public static final Spell frost_axe = placeholder();
	public static final Spell invoke_weather = placeholder();
	public static final Spell chain_lightning = placeholder();
	public static final Spell lightning_bolt = placeholder();
	public static final Spell summon_lightning_wraith = placeholder();
	public static final Spell static_aura = placeholder();
	public static final Spell lightning_disc = placeholder();
	public static final Spell mind_control = placeholder();
	public static final Spell summon_wither_skeleton = placeholder();
	public static final Spell entrapment = placeholder();
	public static final Spell wither_skull = placeholder();
	public static final Spell darkness_orb = placeholder();
	public static final Spell shadow_ward = placeholder();
	public static final Spell decay = placeholder();
	public static final Spell water_breathing = placeholder();
	public static final Spell tornado = placeholder();
	public static final Spell glide = placeholder();
	public static final Spell summon_spirit_horse = placeholder();
	public static final Spell spider_swarm = placeholder();
	public static final Spell slime = placeholder();
	public static final Spell petrify = placeholder();
	public static final Spell invisibility = placeholder();
	public static final Spell levitation = placeholder();
	public static final Spell force_orb = placeholder();
	public static final Spell transportation = placeholder();
	public static final Spell spectral_pathway = placeholder();
	public static final Spell phase_step = placeholder();
	public static final Spell vanishing_box = placeholder();
	public static final Spell greater_heal = placeholder();
	public static final Spell healing_aura = placeholder();
	public static final Spell forcefield = placeholder();
	public static final Spell ironflesh = placeholder();
	public static final Spell transience = placeholder();

	public static final Spell meteor = placeholder();
	public static final Spell fire_breath = placeholder();
	public static final Spell summon_phoenix = placeholder();
	public static final Spell ice_age = placeholder();
	public static final Spell wall_of_frost = placeholder();
	public static final Spell summon_ice_giant = placeholder();
	public static final Spell thunderstorm = placeholder();
	public static final Spell lightning_hammer = placeholder();
	public static final Spell plague_of_darkness = placeholder();
	public static final Spell summon_skeleton_legion = placeholder();
	public static final Spell summon_shadow_wraith = placeholder();
	public static final Spell forests_curse = placeholder();
	public static final Spell flight = placeholder();
	public static final Spell silverfish_swarm = placeholder();
	public static final Spell black_hole = placeholder();
	public static final Spell shockwave = placeholder();
	public static final Spell summon_iron_golem = placeholder();
	public static final Spell arrow_rain = placeholder();
	public static final Spell diamondflesh = placeholder();
	public static final Spell font_of_vitality = placeholder();

	// Wizardry 1.1 spells

	public static final Spell smoke_bomb = placeholder();
	public static final Spell mind_trick = placeholder();
	public static final Spell leap = placeholder();

	public static final Spell pocket_furnace = placeholder();
	public static final Spell intimidate = placeholder();
	public static final Spell banish = placeholder();
	public static final Spell sixth_sense = placeholder();
	public static final Spell darkvision = placeholder();
	public static final Spell clairvoyance = placeholder();
	public static final Spell pocket_workbench = placeholder();
	public static final Spell imbue_weapon = placeholder();
	public static final Spell invigorating_presence = placeholder();
	public static final Spell oakflesh = placeholder();

	public static final Spell greater_fireball = placeholder();
	public static final Spell flaming_weapon = placeholder();
	public static final Spell ice_lance = placeholder();
	public static final Spell freezing_weapon = placeholder();
	public static final Spell ice_spikes = placeholder();
	public static final Spell lightning_pulse = placeholder();
	public static final Spell curse_of_soulbinding = placeholder();
	public static final Spell cobwebs = placeholder();
	public static final Spell decoy = placeholder();
	public static final Spell conjure_armour = placeholder();
	public static final Spell arcane_jammer = placeholder();
	public static final Spell group_heal = placeholder();

	public static final Spell hailstorm = placeholder();
	public static final Spell lightning_web = placeholder();
	public static final Spell summon_storm_elemental = placeholder();
	public static final Spell earthquake = placeholder();
	public static final Spell font_of_mana = placeholder();
	
	// Wizardry 4.2 spells

	public static final Spell mine = placeholder();
	public static final Spell conjure_block = placeholder();
	public static final Spell muffle = placeholder();
	public static final Spell ward = placeholder();
	public static final Spell evade = placeholder();

	public static final Spell iceball = placeholder();
	public static final Spell charge = placeholder();
	public static final Spell reversal = placeholder();
	public static final Spell grapple = placeholder();
	public static final Spell divination = placeholder();
	public static final Spell empowering_presence = placeholder();

	public static final Spell disintegration = placeholder();
	public static final Spell combustion_rune = placeholder();
	public static final Spell frost_step = placeholder();
	public static final Spell paralysis = placeholder();
	public static final Spell shulker_bullet = placeholder();
	public static final Spell curse_of_undeath = placeholder();
	public static final Spell dragon_fireball = placeholder();
	public static final Spell greater_telekinesis = placeholder();
	public static final Spell vex_swarm = placeholder();
	public static final Spell arcane_lock = placeholder();
	public static final Spell containment = placeholder();
	public static final Spell satiety = placeholder();
	public static final Spell greater_ward = placeholder();
	public static final Spell ray_of_purification = placeholder();
	public static final Spell remove_curse = placeholder();

	public static final Spell possession = placeholder();
	public static final Spell curse_of_enfeeblement = placeholder();
	public static final Spell forest_of_thorns = placeholder();
	public static final Spell speed_time = placeholder();
	public static final Spell slow_time = placeholder();
	public static final Spell resurrection = placeholder();

	// Wizardry 4.3 spells

	public static final Spell frost_barrier = placeholder();
	public static final Spell blinding_flash = placeholder();
	public static final Spell enrage = placeholder();
	public static final Spell mark_sacrifice = placeholder();

	public static final Spell permafrost = placeholder();
	public static final Spell stormcloud = placeholder();
	public static final Spell withering_totem = placeholder();
	public static final Spell fangs = placeholder();
	public static final Spell guardian_beam = placeholder();
	public static final Spell radiant_totem = placeholder();

	public static final Spell firestorm = placeholder();
	public static final Spell flamecatcher = placeholder();
	public static final Spell zombie_apocalypse = placeholder();
	public static final Spell boulder = placeholder();
	public static final Spell celestial_smite = placeholder();

	@SubscribeEvent
	public static void register(RegisterEvent event){
        event.register(Spell.registry.get().getRegistryKey(), helper -> {
            helper.register("none", NONE);
            helper.register("magic_missile", MAGIC_MISSILE);
            helper.register("ignite", IGNITE);
            helper.register("freeze", FREEZE);
            helper.register("snowball", SNOWBALL);
            helper.register("arc", ARC);
            helper.register("thunderbolt", THUNDERBOLT);
            helper.register("summon_zombie", SUMMON_ZOMBIE);
            helper.register("snare", SNARE);
            helper.register("dart", DART);
            helper.register("light", LIGHT);
            helper.register("telekinesis", TELEKINESIS);
            helper.register("heal", HEAL);

            helper.register("fireball", FIREBALL);
            helper.register("flame_ray", FLAME_RAY);
            helper.register("firebomb", FIREBOMB);
            helper.register("fire_sigil", FIRE_SIGIL);
            helper.register("firebolt", FIREBOLT);
            helper.register("frost_ray", FROST_RAY);
            helper.register("summon_snow_golem", SUMMON_SNOW_GOLEM);
            helper.register("ice_shard", ICE_SHARD);
            helper.register("ice_statue", ICE_STATUE);
            helper.register("frost_sigil", FROST_SIGIL);
            helper.register("lightning_ray", LIGHTNING_RAY);
            helper.register("spark_bomb", SPARK_BOMB);
            helper.register("homing_spark", HOMING_SPARK);
            helper.register("lightning_sigil", LIGHTNING_SIGIL);
            helper.register("lightning_arrow", LIGHTNING_ARROW);
            helper.register("life_drain", LIFE_DRAIN);
            helper.register("summon_skeleton", SUMMON_SKELETON);
            helper.register("metamorphosis", METAMORPHOSIS);
            helper.register("wither", WITHER);
            helper.register("poison", POISON);
            helper.register("growth_aura", GROWTH_AURA);
            helper.register("bubble", BUBBLE);
            helper.register("whirlwind", WHIRLWIND);
            helper.register("poison_bomb", POISON_BOMB);
            helper.register("summon_spirit_wolf", SUMMON_SPIRIT_WOLF);
            helper.register("blink", BLINK);
            helper.register("agility", AGILITY);
            helper.register("conjure_sword", CONJURE_SWORD);
            helper.register("conjure_pickaxe", CONJURE_PICKAXE);
            helper.register("conjure_bow", CONJURE_BOW);
            helper.register("force_arrow", FORCE_ARROW);
            helper.register("shield", SHIELD);
            helper.register("replenish_hunger", REPLENISH_HUNGER);
            helper.register("cure_effects", CURE_EFFECTS);
            helper.register("heal_ally", HEAL_ALLY);

    		registry.register(new SpellMinion<>("summon_blaze", EntityBlazeMinion::new).soundValues(1, 1.1f, 0.2f));
    		registry.register(new SpellConstruct<>("ring_of_fire", SpellActions.POINT_DOWN, EntityFireRing::new, false).floor(true).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.BURN_DURATION));
    		registry.register(new Detonate());
    		registry.register(new SpellBuff("fire_resistance", 1, 0.5f, 0, () -> MobEffects.FIRE_RESISTANCE).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new SpellBuff("fireskin", 1, 0.5f, 0, () -> WizardryPotions.fireskin).addProperties(Spell.BURN_DURATION));
    		registry.register(new FlamingAxe());
    		registry.register(new SpellConstructRanged<>("blizzard", EntityBlizzard::new, false).addProperties(Spell.EFFECT_RADIUS));
    		registry.register(new SpellMinion<>("summon_ice_wraith", EntityIceWraith::new).soundValues(1, 1.1f, 0.2f));
    		registry.register(new SpellBuff("ice_shroud", 0.3f, 0.5f, 1, () -> WizardryPotions.ice_shroud).addProperties(Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(1, 1.6f, 0.4f));
    		registry.register(new SpellProjectile<>("ice_charge", EntityIceCharge::new).addProperties(Spell.DAMAGE, Spell.EFFECT_RADIUS, Spell.DIRECT_EFFECT_DURATION, Spell.DIRECT_EFFECT_STRENGTH, Spell.SPLASH_EFFECT_DURATION, Spell.SPLASH_EFFECT_STRENGTH, EntityIceCharge.ICE_SHARDS).soundValues(1, 1.6f, 0.4f));
    		registry.register(new FrostAxe());
    		registry.register(new InvokeWeather());
    		registry.register(new ChainLightning());
    		registry.register(new LightningBolt());
    		registry.register(new SpellMinion<>("summon_lightning_wraith", EntityLightningWraith::new).soundValues(1, 1.1f, 0.2f));
    		registry.register(new SpellBuff("static_aura", 0, 0.5f, 0.7f, () -> WizardryPotions.static_aura).addProperties(Spell.DAMAGE).soundValues(1, 1.6f, 0.4f));
    		registry.register(new SpellProjectile<>("lightning_disc", EntityLightningDisc::new).addProperties(Spell.DAMAGE, Spell.SEEKING_STRENGTH).soundValues(1, 0.95f, 0.3f));
    		registry.register(new MindControl());
    		registry.register(new SummonWitherSkeleton());
    		registry.register(new Entrapment());
    		registry.register(new WitherSkull());
    		registry.register(new SpellProjectile<>("darkness_orb", EntityDarknessOrb::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(0.5f, 0.4f, 0.2f));
    		registry.register(new ShadowWard());
    		registry.register(new Decay());
    		registry.register(new SpellBuff("water_breathing", 0.3f, 0.3f, 1, () -> MobEffects.WATER_BREATHING).npcSelector((e, o) -> false).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Tornado());
    		registry.register(new Glide());
    		registry.register(new SummonSpiritHorse());
    		registry.register(new SpellMinion<>("spider_swarm", EntitySpiderMinion::new).soundValues(1, 1.1f, 0.1f));
    		registry.register(new Slime());
    		registry.register(new Petrify());
    		registry.register(new SpellBuff("invisibility", 0.7f, 1, 1, () -> MobEffects.INVISIBILITY).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Levitation());
    		registry.register(new SpellProjectile<>("force_orb", EntityForceOrb::new).addProperties(Spell.DAMAGE, Spell.BLAST_RADIUS).soundValues(0.5f, 0.4f, 0.2f));
    		registry.register(new Transportation());
    		registry.register(new SpectralPathway());
    		registry.register(new PhaseStep());
    		registry.register(new VanishingBox());
    		registry.register(new GreaterHeal());
    		registry.register(new SpellConstruct<>("healing_aura", SpellActions.POINT_DOWN, EntityHealAura::new, false).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.HEALTH));
    		registry.register(new Forcefield());
    		registry.register(new SpellBuff("ironflesh", 0.4f, 0.5f, 0.6f, () -> WizardryPotions.ironflesh).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Transience());

    		registry.register(new Meteor());
    		registry.register(new FireBreath());
    		registry.register(new SpellMinion<>("summon_phoenix", EntityPhoenix::new).flying(true).soundValues(1, 1.1f, 0.1f));
    		registry.register(new IceAge());
    		registry.register(new WallOfFrost());
    		registry.register(new SpellMinion<>("summon_ice_giant", EntityIceGiant::new).soundValues(1, 0.15f, 0.1f));
    		registry.register(new Thunderstorm());
    		registry.register(new LightningHammer());
    		registry.register(new PlagueOfDarkness());
    		registry.register(new SummonSkeletonLegion());
    		registry.register(new SummonShadowWraith());
    		registry.register(new ForestsCurse());
    		registry.register(new Flight());
    		registry.register(new SpellMinion<>("silverfish_swarm", EntitySilverfishMinion::new).soundValues(1, 1.1f, 0.1f));
    		registry.register(new SpellConstructRanged<>("black_hole", EntityBlackHole::new, false).addProperties(Spell.EFFECT_RADIUS).soundValues(2, 0.7f, 0));
    		registry.register(new Shockwave());
    		registry.register(new SpellMinion<>("summon_iron_golem", EntitySpectralGolem::new).soundValues(1, 1.1f, 0.1f));

    		registry.register(new ArrowRain());
    		registry.register(new SpellBuff("diamondflesh", 0.1f, 0.7f, 1, () -> WizardryPotions.diamondflesh).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new SpellBuff("font_of_vitality", 1, 0.8f, 0.3f, () -> MobEffects.ABSORPTION, () -> MobEffects.REGENERATION).soundValues(0.7f, 1.2f, 0.4f));

    		// Wizardry 1.1 spells

    		registry.register(new SpellProjectile<>("smoke_bomb", EntitySmokeBomb::new).addProperties(Spell.BLAST_RADIUS, Spell.EFFECT_DURATION).soundValues(0.5f, 0.4f, 0.2f));
    		registry.register(new MindTrick());
    		registry.register(new Leap());

    		registry.register(new PocketFurnace());
    		registry.register(new Intimidate());
    		registry.register(new Banish());
    		registry.register(new SixthSense());
    		registry.register(new SpellBuff("darkvision", 0, 0.4f, 0.7f, () -> MobEffects.NIGHT_VISION).npcSelector((e, o) -> false).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Clairvoyance());
    		registry.register(new PocketWorkbench());
    		registry.register(new ImbueWeapon());
    		registry.register(new InvigoratingPresence());
    		registry.register(new SpellBuff("oakflesh", 0.6f, 0.5f, 0.4f, () -> WizardryPotions.oakflesh).soundValues(0.7f, 1.2f, 0.4f));

    		registry.register(new SpellProjectile<>("greater_fireball", EntityLargeMagicFireball::new).addProperties(Spell.DAMAGE, EntityLargeMagicFireball.EXPLOSION_POWER));//new GreaterFireball());
    		registry.register(new FlamingWeapon());
    		registry.register(new SpellArrow<>("ice_lance", EntityIceLance::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(1, 1, 0.4f));
    		registry.register(new FreezingWeapon());
    		registry.register(new IceSpikes());
    		registry.register(new LightningPulse());
    		registry.register(new CurseOfSoulbinding());
    		registry.register(new Cobwebs());
    		registry.register(new Decoy());
    		registry.register(new ConjureArmour());
    		registry.register(new ArcaneJammer());
    		registry.register(new GroupHeal());

    		registry.register(new Hailstorm());
    		registry.register(new LightningWeb());
    		registry.register(new SpellMinion<>("summon_storm_elemental", EntityStormElemental::new).soundValues(1, 1.1f, 0.1f));
    		registry.register(new Earthquake());
    		registry.register(new FontOfMana());
    		
    		// Wizardry 4.2 spells
    		
    		registry.register(new Mine());
    		registry.register(new ConjureBlock());
    		registry.register(new SpellBuff("muffle", 0.3f, 0.4f, 0.8f, () -> WizardryPotions.muffle).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new SpellBuff("ward", 0.75f, 0.6f, 0.8f, () -> WizardryPotions.ward).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Evade());

    		registry.register(new SpellProjectile<>("iceball", EntityIceball::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH));
    		registry.register(new Charge());
    		registry.register(new Reversal());
    		registry.register(new Grapple());
    		registry.register(new Divination());
    		registry.register(new EmpoweringPresence());

    		registry.register(new Disintegration());
    		registry.register(new SpellConstructRanged<>("combustion_rune", EntityCombustionRune::new, true).floor(true).addProperties(Spell.BLAST_RADIUS));
    		registry.register(new SpellBuff("frost_step", 0.3f, 0.4f, 0.8f, () -> WizardryPotions.frost_step).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new Paralysis());
    		registry.register(new ShulkerBullet());
    		registry.register(new CurseOfUndeath());
    		registry.register(new DragonFireball());
    		registry.register(new GreaterTelekinesis());
    		registry.register(new SpellMinion<>("vex_swarm", EntityVexMinion::new).flying(true).soundValues(1, 1.1f, 0.1f));
    		registry.register(new ArcaneLock());
    		registry.register(new Containment());
    		registry.register(new Satiety());
    		registry.register(new SpellBuff("greater_ward", 0.75f, 0.6f, 0.8f, () -> WizardryPotions.ward).soundValues(0.7f, 1.2f, 0.4f));
    		registry.register(new RayOfPurification());
    		registry.register(new RemoveCurse());

    		registry.register(new Possession());
    		registry.register(new CurseOfEnfeeblement());
    		registry.register(new ForestOfThorns());
    		registry.register(new SpeedTime());
    		registry.register(new SlowTime());
    		registry.register(new Resurrection());

    		// Wizardry 4.3 spells

    		registry.register(new FrostBarrier());

    		registry.register(new BlindingFlash());
    		registry.register(new Enrage());
    		registry.register(new MarkSacrifice());

    		registry.register(new Permafrost());
    		registry.register(new Stormcloud());
    		registry.register(new WitheringTotem());
    		registry.register(new Fangs());
    		registry.register(new GuardianBeam());
    		registry.register(new SpellBuff("mirage", 0.64f, 0.47f, 0.9f, () -> WizardryPotions.mirage));
    		registry.register(new RadiantTotem());

    		registry.register(new Firestorm());
    		registry.register(new Flamecatcher());
    		registry.register(new ZombieApocalypse());
    		registry.register(new Boulder());
    		registry.register(new CelestialSmite());
        });

	}

}