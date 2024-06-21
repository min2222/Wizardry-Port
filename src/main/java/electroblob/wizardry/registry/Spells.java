package electroblob.wizardry.registry;

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

    public static final Spell SUMMON_BLAZE = new SpellMinion<>("summon_blaze", EntityBlazeMinion::new).soundValues(1, 1.1f, 0.2f);
    public static final Spell RING_OF_FIRE = new SpellConstruct<>("ring_of_fire", SpellActions.POINT_DOWN, EntityFireRing::new, false).floor(true).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.BURN_DURATION);
    public static final Spell DETONATE = new Detonate();
    public static final Spell FIRE_RESISTANCE = new SpellBuff("fire_resistance", 1, 0.5f, 0, () -> MobEffects.FIRE_RESISTANCE).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell FIRESKIN = new SpellBuff("fireskin", 1, 0.5f, 0, () -> WizardryPotions.FIRESKIN.get()).addProperties(Spell.BURN_DURATION);
    public static final Spell FLAMING_AXE = new FlamingAxe();
    public static final Spell BLIZZARD = new SpellConstructRanged<>("blizzard", EntityBlizzard::new, false).addProperties(Spell.EFFECT_RADIUS);
    public static final Spell SUMMON_ICE_WRAITH = new SpellMinion<>("summon_ice_wraith", EntityIceWraith::new).soundValues(1, 1.1f, 0.2f);
    public static final Spell ICE_SHROUD = new SpellBuff("ice_shroud", 0.3f, 0.5f, 1, () -> WizardryPotions.ICE_SHROUD.get()).addProperties(Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(1, 1.6f, 0.4f);
    public static final Spell ICE_CHARGE = new SpellProjectile<>("ice_charge", EntityIceCharge::new).addProperties(Spell.DAMAGE, Spell.EFFECT_RADIUS, Spell.DIRECT_EFFECT_DURATION, Spell.DIRECT_EFFECT_STRENGTH, Spell.SPLASH_EFFECT_DURATION, Spell.SPLASH_EFFECT_STRENGTH, EntityIceCharge.ICE_SHARDS).soundValues(1, 1.6f, 0.4f);
    public static final Spell FROST_AXE = new FrostAxe();
    public static final Spell INVOKE_WEATHER = new InvokeWeather();
    public static final Spell CHAIN_LIGHTNING = new ChainLightning();
    public static final Spell LIGHTNING_BOLT = new LightningBolt();
    public static final Spell SUMMON_LIGHTNING_WRAITH = new SpellMinion<>("summon_lightning_wraith", EntityLightningWraith::new).soundValues(1, 1.1f, 0.2f);
    public static final Spell STATIC_AURA = new SpellBuff("static_aura", 0, 0.5f, 0.7f, () -> WizardryPotions.STATIC_AURA.get()).addProperties(Spell.DAMAGE).soundValues(1, 1.6f, 0.4f);
    public static final Spell LIGHTNING_DISC = new SpellProjectile<>("lightning_disc", EntityLightningDisc::new).addProperties(Spell.DAMAGE, Spell.SEEKING_STRENGTH).soundValues(1, 0.95f, 0.3f);
    public static final Spell MIND_CONTROL = new MindControl();
    public static final Spell SUMMON_WITHER_SKELETON = new SummonWitherSkeleton();
    public static final Spell ENTRAPMENT = new Entrapment();
    public static final Spell WITHER_SKULL = new WitherSkull();
    public static final Spell DARKNESS_ORB = new SpellProjectile<>("darkness_orb", EntityDarknessOrb::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell SHADOW_WARD = new ShadowWard();
    public static final Spell DECAY = new Decay();
    public static final Spell WATER_BREATHING = new SpellBuff("water_breathing", 0.3f, 0.3f, 1, () -> MobEffects.WATER_BREATHING).npcSelector((e, o) -> false).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell TORNADO = new Tornado();
    public static final Spell GLIDE = new Glide();
    public static final Spell SUMMON_SPIRIT_HORSE = new SummonSpiritHorse();
    public static final Spell SPIDER_SWARM = new SpellMinion<>("spider_swarm", EntitySpiderMinion::new).soundValues(1, 1.1f, 0.1f);
    public static final Spell SLIME = new Slime();
    public static final Spell PETRIFY = new Petrify();
    public static final Spell INVISIBLITY = new SpellBuff("invisibility", 0.7f, 1, 1, () -> MobEffects.INVISIBILITY).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell LEVITATION = new Levitation();
    public static final Spell FORCE_ORB = new SpellProjectile<>("force_orb", EntityForceOrb::new).addProperties(Spell.DAMAGE, Spell.BLAST_RADIUS).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell TRANSPORTATION = new Transportation();
    public static final Spell SPECTRAL_PATHWAY = new SpectralPathway();
    public static final Spell PHASE_STEP = new PhaseStep();
    public static final Spell VANISHING_BOX = new VanishingBox();
    public static final Spell GREATER_HEAL = new GreaterHeal();
    public static final Spell HEALING_AURA = new SpellConstruct<>("healing_aura", SpellActions.POINT_DOWN, EntityHealAura::new, false).addProperties(Spell.EFFECT_RADIUS, Spell.DAMAGE, Spell.HEALTH);
    public static final Spell FORCEFIELD = new Forcefield();
    public static final Spell IRONFLESH = new SpellBuff("ironflesh", 0.4f, 0.5f, 0.6f, () -> WizardryPotions.IRONFLESH.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell TRANSIENCE = new Transience();

    public static final Spell METEOR = new Meteor();
    public static final Spell FIRE_BREATH = new FireBreath();
    public static final Spell SUMMON_PHOENIX = new SpellMinion<>("summon_phoenix", EntityPhoenix::new).flying(true).soundValues(1, 1.1f, 0.1f);
    public static final Spell ICE_AGE = new IceAge();
    public static final Spell WALL_OF_FROST = new WallOfFrost();
    public static final Spell SUMMON_ICE_GIANT = new SpellMinion<>("summon_ice_giant", EntityIceGiant::new).soundValues(1, 0.15f, 0.1f);
    public static final Spell THUNDER_STORM = new Thunderstorm();
    public static final Spell LIGHTNING_HAMMER = new LightningHammer();
    public static final Spell PLAGUE_OF_DARKNESS = new PlagueOfDarkness();
    public static final Spell SUMMON_SKELETON_LEGION = new SummonSkeletonLegion();
    public static final Spell SUMMON_SHADOW_WRAITH = new SummonShadowWraith();
    public static final Spell FORESTS_CURSE = new ForestsCurse();
    public static final Spell FLIGHT = new Flight();
    public static final Spell SILVERFISH_SWARM = new SpellMinion<>("silverfish_swarm", EntitySilverfishMinion::new).soundValues(1, 1.1f, 0.1f);
    public static final Spell BLACK_HOLE = new SpellConstructRanged<>("black_hole", EntityBlackHole::new, false).addProperties(Spell.EFFECT_RADIUS).soundValues(2, 0.7f, 0);
    public static final Spell SHOCKWAVE = new Shockwave();
    public static final Spell SUMMON_IRON_GOLEM = new SpellMinion<>("summon_iron_golem", EntitySpectralGolem::new).soundValues(1, 1.1f, 0.1f);
    public static final Spell ARROW_RAIN = new ArrowRain();
    public static final Spell DIAMONDFLESH = new SpellBuff("diamondflesh", 0.1f, 0.7f, 1, () -> WizardryPotions.DIAMONDFLESH.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell FONT_OF_VITALITY = new SpellBuff("font_of_vitality", 1, 0.8f, 0.3f, () -> MobEffects.ABSORPTION, () -> MobEffects.REGENERATION).soundValues(0.7f, 1.2f, 0.4f);

	// Wizardry 1.1 spells

    public static final Spell SMOKE_BOMB = new SpellProjectile<>("smoke_bomb", EntitySmokeBomb::new).addProperties(Spell.BLAST_RADIUS, Spell.EFFECT_DURATION).soundValues(0.5f, 0.4f, 0.2f);
    public static final Spell MIND_TRICK = new MindTrick();
    public static final Spell LEAP = new Leap();

    public static final Spell POCKET_FURNACE = new PocketFurnace();
    public static final Spell INTIMIDATE = new Intimidate();
    public static final Spell BANISH = new Banish();
    public static final Spell SIXTH_SENSE = new SixthSense();
    public static final Spell DARKVISION = new SpellBuff("darkvision", 0, 0.4f, 0.7f, () -> MobEffects.NIGHT_VISION).npcSelector((e, o) -> false).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell CLAIRVOYANCE = new Clairvoyance();
    public static final Spell POCKET_WORKBENCH = new PocketWorkbench();
    public static final Spell IMBUE_WEAPON = new ImbueWeapon();
    public static final Spell INVIGORATING_PRESENCE = new InvigoratingPresence();
    public static final Spell OAKFLESH = new SpellBuff("oakflesh", 0.6f, 0.5f, 0.4f, () -> WizardryPotions.OAKFLESH.get()).soundValues(0.7f, 1.2f, 0.4f);

    public static final Spell GREATER_FIREBALL = new SpellProjectile<>("greater_fireball", EntityLargeMagicFireball::new).addProperties(Spell.DAMAGE, EntityLargeMagicFireball.EXPLOSION_POWER);
    public static final Spell FLAMING_WEAPON = new FlamingWeapon();
    public static final Spell ICE_LANCE = new SpellArrow<>("ice_lance", EntityIceLance::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH).soundValues(1, 1, 0.4f);
    public static final Spell FREEZING_WEAPON = new FreezingWeapon();
    public static final Spell ICE_SPIKES = new IceSpikes();
    public static final Spell LIGHTNING_PULSE = new LightningPulse();
    public static final Spell CURSE_OF_SOULBINDING = new CurseOfSoulbinding();
    public static final Spell COBWEBS = new Cobwebs();
    public static final Spell DECOY = new Decoy();
    public static final Spell CONJURE_ARMOUR = new ConjureArmour();
    public static final Spell ARCANE_JAMMER = new ArcaneJammer();
    public static final Spell GROUP_HEAL = new GroupHeal();

    public static final Spell HAILSTORM = new Hailstorm();
    public static final Spell LIGHTNING_WEB = new LightningWeb();
    public static final Spell SUMMON_STORM_ELEMENTAL = new SpellMinion<>("summon_storm_elemental", EntityStormElemental::new).soundValues(1, 1.1f, 0.1f);
    public static final Spell EARTHQUAKE = new Earthquake();
    public static final Spell FONT_OF_MANA = new FontOfMana();
	
	// Wizardry 4.2 spells

    public static final Spell MINE = new Mine();
    public static final Spell CONJURE_BLOCKS = new ConjureBlock();
    public static final Spell MUFFLE = new SpellBuff("muffle", 0.3f, 0.4f, 0.8f, () -> WizardryPotions.MUFFLE.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell WARD = new SpellBuff("ward", 0.75f, 0.6f, 0.8f, () -> WizardryPotions.WARD.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell EVADE = new Evade();

    public static final Spell ICEBALL = new SpellProjectile<>("iceball", EntityIceball::new).addProperties(Spell.DAMAGE, Spell.EFFECT_DURATION, Spell.EFFECT_STRENGTH);
    public static final Spell CHARGE = new Charge();
    public static final Spell REVERSAL = new Reversal();
    public static final Spell GRAPPLE = new Grapple();
    public static final Spell DIVINATION = new Divination();
    public static final Spell EMPOWERING_PRESENCE = new EmpoweringPresence();

    public static final Spell DISINTEGRATION = new Disintegration();
    public static final Spell COMBUSTION_RUNE = new SpellConstructRanged<>("combustion_rune", EntityCombustionRune::new, true).floor(true).addProperties(Spell.BLAST_RADIUS);
    public static final Spell FROST_STEP = new SpellBuff("frost_step", 0.3f, 0.4f, 0.8f, () -> WizardryPotions.FROST_STEP.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell PARALYSIS = new Paralysis();
    public static final Spell SHULKER_BULLET = new ShulkerBullet();
    public static final Spell CURSE_OF_UNDEATH = new CurseOfUndeath();
    public static final Spell DRAGON_FIREBALL = new DragonFireball();
    public static final Spell GREATER_TELEKINESIS = new GreaterTelekinesis();
    public static final Spell VEX_SWARM = new SpellMinion<>("vex_swarm", EntityVexMinion::new).flying(true).soundValues(1, 1.1f, 0.1f);
    public static final Spell ARCANE_LOCK = new ArcaneLock();
    public static final Spell CONTAINMENT = new Containment();
    public static final Spell SATIETY = new Satiety();
    public static final Spell GREATER_WARD = new SpellBuff("greater_ward", 0.75f, 0.6f, 0.8f, () -> WizardryPotions.WARD.get()).soundValues(0.7f, 1.2f, 0.4f);
    public static final Spell RAY_OF_PURIFICATION = new RayOfPurification();
    public static final Spell REMOVE_CURSE = new RemoveCurse();

    public static final Spell POSSESSION = new Possession();
    public static final Spell CURSE_OF_ENFEEBLEMENT = new CurseOfEnfeeblement();
    public static final Spell FOREST_OF_THORNS = new ForestOfThorns();
    public static final Spell SPEED_TIME = new SpeedTime();
    public static final Spell SLOW_TIME = new SlowTime();
    public static final Spell RESURRECTION = new Resurrection();

	// Wizardry 4.3 spells

    public static final Spell FROST_BARRIER = new FrostBarrier();
    public static final Spell BLINDING_FLASH = new BlindingFlash();
    public static final Spell ENRAGE = new Enrage();
    public static final Spell MARK_SACRIFICE = new MarkSacrifice();

    public static final Spell PERMAFROST = new Permafrost();
    public static final Spell STORMCLOUD = new Stormcloud();
    public static final Spell WITHERING_TOTEM = new WitheringTotem();
    public static final Spell FANGS = new Fangs();
    public static final Spell GUARDIAN_BEAM = new GuardianBeam();
    public static final Spell MIRAGE = new SpellBuff("mirage", 0.64f, 0.47f, 0.9f, () -> WizardryPotions.MIRAGE.get());
    public static final Spell RADIANT_TOTEM = new RadiantTotem();

    public static final Spell FIRESTORM = new Firestorm();
    public static final Spell FLAMECATCHER = new Flamecatcher();
    public static final Spell ZOMBIE_APOCALYPSE = new ZombieApocalypse();
    public static final Spell BOULDER = new Boulder();
    public static final Spell CELESTIAL_SMITE = new CelestialSmite();

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

            helper.register("summon_blaze", SUMMON_BLAZE);
            helper.register("ring_of_fire", RING_OF_FIRE);
            helper.register("detonate", DETONATE);
            helper.register("fire_resistance", FIRE_RESISTANCE);
            helper.register("fireskin", FIRESKIN);
            helper.register("flaming_axe", FLAMING_AXE);
            helper.register("blizzard", BLIZZARD);
            helper.register("summon_ice_wraith", SUMMON_ICE_WRAITH);
            helper.register("ice_shroud", ICE_SHROUD);
            helper.register("ice_charge", ICE_CHARGE);
            helper.register("frost_axe", FROST_AXE);
            helper.register("invoke_weather", INVOKE_WEATHER);
            helper.register("chain_lightning", CHAIN_LIGHTNING);
            helper.register("lightning_bolt", LIGHTNING_BOLT);
            helper.register("summon_lightning_wraith", SUMMON_LIGHTNING_WRAITH);
            helper.register("static_aura", STATIC_AURA);
            helper.register("lightning_disc", LIGHTNING_DISC);
            helper.register("mind_control", MIND_CONTROL);
            helper.register("summon_wither_skeleton", SUMMON_WITHER_SKELETON);
            helper.register("entrapment", ENTRAPMENT);
            helper.register("wither_skull", WITHER_SKULL);
            helper.register("darkness_orb", DARKNESS_ORB);
            helper.register("shadow_ward", SHADOW_WARD);
            helper.register("decay", DECAY);
            helper.register("water_breathing", WATER_BREATHING);
            helper.register("tornado", TORNADO);
            helper.register("glide", GLIDE);
            helper.register("summon_spirit_horse", SUMMON_SPIRIT_HORSE);
            helper.register("spider_swarm", SPIDER_SWARM);
            helper.register("slime", SLIME);
            helper.register("petrify", PETRIFY);
            helper.register("invisibility", INVISIBLITY);
            helper.register("levitation", LEVITATION);
            helper.register("force_orb", FORCE_ORB);
            helper.register("transportation", TRANSPORTATION);
            helper.register("spectral_pathway", SPECTRAL_PATHWAY);
            helper.register("phase_step", PHASE_STEP);
            helper.register("vanishing_box", VANISHING_BOX);
            helper.register("greater_heal", GREATER_HEAL);
            helper.register("healing_aura", HEALING_AURA);
            helper.register("forcefield", FORCEFIELD);
            helper.register("ironflesh", IRONFLESH);
            helper.register("transience", TRANSIENCE);

            helper.register("meteor", METEOR);
            helper.register("fire_breath", FIRE_BREATH);
            helper.register("summon_phoenix", SUMMON_PHOENIX);
            helper.register("ice_age", ICE_AGE);
            helper.register("wall_of_frost", WALL_OF_FROST);
            helper.register("summon_ice_giant", SUMMON_ICE_GIANT);
            helper.register("thunder_storm", THUNDER_STORM);
            helper.register("lightning_hammer", LIGHTNING_HAMMER);
            helper.register("plague_of_darkness", PLAGUE_OF_DARKNESS);
            helper.register("summon_skeleton_legion", SUMMON_SKELETON_LEGION);
            helper.register("summon_shadow_wraith", SUMMON_SHADOW_WRAITH);
            helper.register("forests_curse", FORESTS_CURSE);
            helper.register("flight", FLIGHT);
            helper.register("silverfish_swarm", SILVERFISH_SWARM);
            helper.register("black_hole", BLACK_HOLE);
            helper.register("shockwave", SHOCKWAVE);
            helper.register("summon_iron_golem", SUMMON_IRON_GOLEM);

            helper.register("arrow_rain", ARROW_RAIN);
            helper.register("diamondflesh", DIAMONDFLESH);
            helper.register("font_of_vitality", FONT_OF_VITALITY);

    		// Wizardry 1.1 spells

            helper.register("smoke_bomb", SMOKE_BOMB);
            helper.register("mind_trick", MIND_TRICK);
            helper.register("leap", LEAP);

            helper.register("pocket_furnace", POCKET_FURNACE);
            helper.register("intimidate", INTIMIDATE);
            helper.register("banish", BANISH);
            helper.register("sixth_sense", SIXTH_SENSE);
            helper.register("darkvision", DARKVISION);
            helper.register("clairvoyance", CLAIRVOYANCE);
    		helper.register("pocket_workbench", POCKET_WORKBENCH);
            helper.register("imbue_weapon", IMBUE_WEAPON);
            helper.register("invigorating_presence", INVIGORATING_PRESENCE);
    		helper.register("oakflesh", OAKFLESH);

            helper.register("greater_fireball", GREATER_FIREBALL);
            helper.register("flaming_weapon", FLAMING_WEAPON);
            helper.register("ice_lance", ICE_LANCE);
            helper.register("freezing_weapon", FREEZING_WEAPON);
            helper.register("ice_spikes", ICE_SPIKES);
            helper.register("lightning_pulse", LIGHTNING_PULSE);
            helper.register("curse_of_soulbinding", CURSE_OF_SOULBINDING);
            helper.register("cobwebs", COBWEBS);
            helper.register("decoy", DECOY);
            helper.register("conjure_armour", CONJURE_ARMOUR);
            helper.register("arcane_jammer", ARCANE_JAMMER);
            helper.register("group_heal", GROUP_HEAL);

            helper.register("hailstorm", HAILSTORM);
            helper.register("lightning_web", LIGHTNING_WEB);
            helper.register("summon_storm_elemental", SUMMON_STORM_ELEMENTAL);
            helper.register("earthquake", EARTHQUAKE);
            helper.register("font_of_mana", FONT_OF_MANA);
    		
    		// Wizardry 4.2 spells
    		
            helper.register("mine", MINE);
            helper.register("conjure_blocks", CONJURE_BLOCKS);
            helper.register("muffle", MUFFLE);
            helper.register("ward", WARD);
            helper.register("evade", EVADE);

            helper.register("iceball", ICEBALL);
            helper.register("charge", CHARGE);
            helper.register("reversal", REVERSAL);
            helper.register("grapple", GRAPPLE);
            helper.register("divination", DIVINATION);
            helper.register("empowering_presence", EMPOWERING_PRESENCE);

            helper.register("disintegration", DISINTEGRATION);
            helper.register("combustion_rune", COMBUSTION_RUNE);
            helper.register("frost_step", FROST_STEP);
            helper.register("paralysis", PARALYSIS);
            helper.register("shulker_bullet", SHULKER_BULLET);
            helper.register("curse_of_undeath", CURSE_OF_UNDEATH);
            helper.register("dragon_fireball", DRAGON_FIREBALL);
            helper.register("greater_telekinesis", GREATER_TELEKINESIS);
            helper.register("vex_swarm", VEX_SWARM);
            helper.register("arcane_lock", ARCANE_LOCK);
            helper.register("containment", CONTAINMENT);
            helper.register("satiety", SATIETY);
            helper.register("greater_ward", GREATER_WARD);
            helper.register("ray_of_purification", RAY_OF_PURIFICATION);
            helper.register("remove_curse", REMOVE_CURSE);

            helper.register("possession", POSSESSION);
            helper.register("curse_of_enfeeblement", CURSE_OF_ENFEEBLEMENT);
            helper.register("forest_of_thorns", FOREST_OF_THORNS);
            helper.register("speed_time", SPEED_TIME);
            helper.register("slow_time", SLOW_TIME);
            helper.register("resurrection", RESURRECTION);

    		// Wizardry 4.3 spells

            helper.register("frost_barrier", FROST_BARRIER);

            helper.register("blinding_flash", BLINDING_FLASH);
            helper.register("enrage", ENRAGE);
            helper.register("mark_sacrifice", MARK_SACRIFICE);

            helper.register("permafrost", PERMAFROST);
            helper.register("stormcloud", STORMCLOUD);
            helper.register("withering_totem", WITHERING_TOTEM);
            helper.register("fangs", FANGS);
            helper.register("guardian_beam", GUARDIAN_BEAM);
            helper.register("mirage", MIRAGE);
            helper.register("radiant_totem", RADIANT_TOTEM);

            helper.register("firestorm", FIRESTORM);
            helper.register("flamecatcher", FLAMECATCHER);
            helper.register("zombie_apocalypse", ZOMBIE_APOCALYPSE);
            helper.register("boulder", BOULDER);
            helper.register("celestial_smite", CELESTIAL_SMITE);
        });

	}

}