package electroblob.wizardry.registry;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.min01.simplestmobs.SimplestMobs;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.construct.EntityArrowRain;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityBoulder;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.entity.construct.EntityCombustionRune;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityFireSigil;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.construct.EntityRadiantTotem;
import electroblob.wizardry.entity.construct.EntityStormcloud;
import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.entity.construct.EntityWitheringTotem;
import electroblob.wizardry.entity.construct.EntityZombieSpawner;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityHuskMinion;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityMagicSlime;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntityRemnant;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntitySpectralGolem;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityStrayMinion;
import electroblob.wizardry.entity.living.EntityVexMinion;
import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import electroblob.wizardry.entity.projectile.EntityDart;
import electroblob.wizardry.entity.projectile.EntityEmber;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityFlamecatcherArrow;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EnumCreatureType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class responsible for registering all of wizardry's entities and their spawning conditions.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
@Mod.EventBusSubscriber
public class WizardryEntities {

	private WizardryEntities(){} // No instances!

	/** Most entity trackers fall into one of a few categories, so they are defined here for convenience. This
	 * generally follows the values used in vanilla for each entity type. */
	enum TrackingType {

		LIVING(80, 3, true),
		PROJECTILE(64, 10, true),
		CONSTRUCT(160, 10, false);

		int range;
		int interval;
		boolean trackVelocity;

		TrackingType(int range, int interval, boolean trackVelocity){
			this.range = range;
			this.interval = interval;
			this.trackVelocity = trackVelocity;
		}
	}
	
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Wizardry.MODID);

	// Vanilla summoned creatures
	public static final RegistryObject<EntityType<EntityZombieMinion>> ZOMBIE_MINION = ENTITY_TYPES.register(createEntry(EntityZombieMinion::new, "zombie_minion", MobCategory.MONSTER, TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityHuskMinion>> HUSK_MINION = ENTITY_TYPES.register(createEntry(EntityHuskMinion::new, "husk_minion", MobCategory.MONSTER, TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySkeletonMinion>> SKELETON_MINION = ENTITY_TYPES.register(createEntry(EntitySkeletonMinion::new, "skeleton_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityStrayMinion>> STRAY_MINION = ENTITY_TYPES.register(createEntry(EntityStrayMinion::new, "stray_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySpiderMinion>> SPIDER_MINION = ENTITY_TYPES.register(createEntry(EntitySpiderMinion::new, "spider_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityBlazeMinion>> BLAZE_MINION = ENTITY_TYPES.register(createEntry(EntityBlazeMinion::new, "blaze_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityWitherSkeletonMinion>> WITHER_SKELETON_MINION = ENTITY_TYPES.register(createEntry(EntityWitherSkeletonMinion::new, "wither_skeleton_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySilverfishMinion>> SILVERFISH_MINION = ENTITY_TYPES.register(createEntry(EntitySilverfishMinion::new, "silverfish_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityVexMinion>> VEX_MINION = ENTITY_TYPES.register(createEntry(EntityVexMinion::new, "vex_minion", TrackingType.LIVING));

	// Custom summoned creatures
	public static final RegistryObject<EntityType<EntityIceWraith>> ICE_WRAITH = ENTITY_TYPES.register(createEntry(EntityIceWraith::new, "ice_wraith", TrackingType.LIVING).egg(0xaafaff, 0x001ce1)
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.iceWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValues().stream()
					.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName())
							&& BiomeDictionary.hasType(b, BiomeDictionary.Type.SNOWY)
							&& !BiomeDictionary.hasType(b, BiomeDictionary.Type.FOREST))
					.collect(Collectors.toSet())));

	public static final RegistryObject<EntityType<EntityLightningWraith>> LIGHTNING_WRAITH = ENTITY_TYPES.register(createEntry(EntityLightningWraith::new, 	"lightning_wraith", 	TrackingType.LIVING).egg(0x35424b, 0x27b9d9)
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.lightningWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValuesCollection().stream()
				.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
				.collect(Collectors.toSet())));

	public static final RegistryObject<EntityType<EntitySpiritWolf>> SPIRIT_WOLF = ENTITY_TYPES.register(createEntry(EntitySpiritWolf::new, 		"spirit_wolf", 		TrackingType.LIVING).egg(0xbcc2e8, 0x5464c6));
	public static final RegistryObject<EntityType<EntitySpiritHorse>> SPIRIT_HORSE = ENTITY_TYPES.register(createEntry(EntitySpiritHorse::new, 		"spirit_horse", 		TrackingType.LIVING).egg(0x5464c6, 0xbcc2e8));
	public static final RegistryObject<EntityType<EntityPhoenix>> PHOENIX = ENTITY_TYPES.register(createEntry(EntityPhoenix::new, 			"phoenix", 			TrackingType.LIVING).egg(0xff4900, 0xfde535));
	public static final RegistryObject<EntityType<EntityIceGiant>> ICE_GIANT = ENTITY_TYPES.register(createEntry(EntityIceGiant::new, 		"ice_giant", 			TrackingType.LIVING).egg(0x5bacd9, 0xeffaff));
	public static final RegistryObject<EntityType<EntitySpectralGolem>> SPECTRAL_GOLEM = ENTITY_TYPES.register(createEntry(EntitySpectralGolem::new,	"spectral_golem",		TrackingType.LIVING).egg(0x5bacd9, 0xeffaff));

	public static final RegistryObject<EntityType<EntityMagicSlime>> MAGIC_SLIME = ENTITY_TYPES.register(createEntry(EntityMagicSlime::new, 		"magic_slime", 		TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityDecoy>> DECOY = ENTITY_TYPES.register(createEntry(EntityDecoy::new, 			"decoy", 				TrackingType.LIVING));

	// These two are only made of particles, so we can afford a lower update frequency
	public static final RegistryObject<EntityType<EntityShadowWraith>> SHADOW_WRAITH = ENTITY_TYPES.register(createEntry(EntityShadowWraith::new, 	"shadow_wraith")		.tracker(80, 10, true).egg(0x11071c, 0x421384));
	public static final RegistryObject<EntityType<EntityStormElemental>> STORM_ELEMENTAL = ENTITY_TYPES.register(createEntry(EntityStormElemental::new, 	"storm_elemental")	.tracker(80, 10, true).egg(0x162128, 0x135279));

	// Other living entities
	public static final RegistryObject<EntityType<EntityWizard>> WIZARD = ENTITY_TYPES.register(createEntry(EntityWizard::new, 			"wizard", 			TrackingType.LIVING).egg(0x19295e, 0xee9312));
	public static final RegistryObject<EntityType<EntityEvilWizard>> EVIL_WIZARD = ENTITY_TYPES.register(createEntry(EntityEvilWizard::new, 		"evil_wizard", 		TrackingType.LIVING).egg(0x290404, 0xee9312)
			// For reference: 5, 1, 1 are the parameters for the witch in vanilla
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.evilWizardSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValues().stream()
					.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
					.collect(Collectors.toSet())));
	public static final RegistryObject<EntityType<EntityRemnant>> REMNANT = ENTITY_TYPES.register(createEntry(EntityRemnant::new, 			"remnant", 			TrackingType.LIVING).egg(0x414141, 0xe5daae));

	// Directed projectiles
	public static final RegistryObject<EntityType<EntityMagicMissile>> MAGIC_MISSILE = ENTITY_TYPES.register(createEntry(EntityMagicMissile::new, 	"magic_missile", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceShard>> ICE_SHARD = ENTITY_TYPES.register(createEntry(EntityIceShard::new, 		"ice_shard", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityLightningArrow>> LIGHTNING_ARROW = ENTITY_TYPES.register(createEntry(EntityLightningArrow::new, 	"lightning_arrow", 	TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityForceArrow>> FORCE_ARROW = ENTITY_TYPES.register(createEntry(EntityForceArrow::new, 		"force_arrow", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityDart>> DART = ENTITY_TYPES.register(createEntry(EntityDart::new, 			"dart", 				TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceLance>> ICE_LANCE = ENTITY_TYPES.register(createEntry(EntityIceLance::new, 		"ice_lance", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityFlamecatcherArrow>> FLAMECATCHER_ARROW = ENTITY_TYPES.register(createEntry(EntityFlamecatcherArrow::new, "flamecatcher_arrow", TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityConjuredArrow>> CONJURED_ARROW = ENTITY_TYPES.register(createEntry(EntityConjuredArrow::new, "conjured_arrow", TrackingType.PROJECTILE));

	// Directionless projectiles
	public static final RegistryObject<EntityType<EntityFirebomb>> FIREBOMB = ENTITY_TYPES.register(createEntry(EntityFirebomb::new, 		"firebomb", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityPoisonBomb>> POISON_BOMB = ENTITY_TYPES.register(createEntry(EntityPoisonBomb::new, 		"poison_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySparkBomb>> SPARK_BOMB = ENTITY_TYPES.register(createEntry(EntitySparkBomb::new, 		"spark_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySmokeBomb>> SMOKE_BOMB = ENTITY_TYPES.register(createEntry(EntitySmokeBomb::new, 		"smoke_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceCharge>> ICE_CHARGE = ENTITY_TYPES.register(createEntry(EntityIceCharge::new, 		"ice_charge", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityForceOrb>> FORCE_ORB = ENTITY_TYPES.register(createEntry(EntityForceOrb::new, 		"force_orb", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySpark>> SPARK = ENTITY_TYPES.register(createEntry(EntitySpark::new, 			"spark", 				TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityDarknessOrb>> DARKNESS_ORB = ENTITY_TYPES.register(createEntry(EntityDarknessOrb::new, 		"darkness_orb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityFirebolt>> FIREBOLT = ENTITY_TYPES.register(createEntry(EntityFirebolt::new, 		"firebolt", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityThunderbolt>> THUNDERBOLT = ENTITY_TYPES.register(createEntry(EntityThunderbolt::new, 		"thunderbolt", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityLightningDisc>> LIGHTNING_DISC = ENTITY_TYPES.register(createEntry(EntityLightningDisc::new, 	"lightning_disc", 	TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityEmber>> EMBER = ENTITY_TYPES.register(createEntry(EntityEmber::new, 			"ember", 				TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityMagicFireball>> MAGIC_FIREBALL = ENTITY_TYPES.register(createEntry(EntityMagicFireball::new, 	"magic_fireball", 	TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityLargeMagicFireball>> LARGE_MAGIC_FIREBALL = ENTITY_TYPES.register(createEntry(EntityLargeMagicFireball::new, "large_magic_fireball", TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceball>> ICEBALL = ENTITY_TYPES.register(createEntry(EntityIceball::new, 			"iceball", MobCategory.MISC, TrackingType.PROJECTILE));

	// These are effectively projectiles, but since they're bigger and start high up they need updating from further away
	public static final RegistryObject<EntityType<EntityMeteor>> METEOR = ENTITY_TYPES.register(createEntry(EntityMeteor::new, 			"meteor", MobCategory.MISC, 160, 3, true));
	public static final RegistryObject<EntityType<EntityHammer>> LIGHTNING_HAMMER = ENTITY_TYPES.register(createEntry(EntityHammer::new, 			"lightning_hammer", MobCategory.MISC, 160, 3, true));
	public static final RegistryObject<EntityType<EntityLevitatingBlock>> LEVITATING_BLOCK = ENTITY_TYPES.register(createEntry(EntityLevitatingBlock::new, 	"levitating_block", MobCategory.MISC, 160, 3, true));

	// Constructs
	public static final RegistryObject<EntityType<EntityBlackHole>> BLACK_HOLE = ENTITY_TYPES.register(createEntry(EntityBlackHole::new, 		"black_hole", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityBlizzard>> BLIZZARD = ENTITY_TYPES.register(createEntry(EntityBlizzard::new, 		"blizzard", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityForcefield>> FORCEFIELD = ENTITY_TYPES.register(createEntry(EntityForcefield::new, 		"forcefield", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFireSigil>> FIRE_SIGIL = ENTITY_TYPES.register(createEntry(EntityFireSigil::new, 		"fire_sigil", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFrostSigil>> FROST_SIGIL = ENTITY_TYPES.register(createEntry(EntityFrostSigil::new, 		"frost_sigil", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityLightningSigil>> LIGHTNING_SIGIL = ENTITY_TYPES.register(createEntry(EntityLightningSigil::new, 	"lightning_sigil", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityCombustionRune>> COMBUSTION_RUNE = ENTITY_TYPES.register(createEntry(EntityCombustionRune::new, 	"combustion_rune", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFireRing>> RING_OF_FIRE = ENTITY_TYPES.register(createEntry(EntityFireRing::new, 		"ring_of_fire", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityHealAura>> HEALING_AURA = ENTITY_TYPES.register(createEntry(EntityHealAura::new, 		"healing_aura", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityDecay>> DECAY = ENTITY_TYPES.register(createEntry(EntityDecay::new, 			"decay", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityZombieSpawner>> ZOMBIE_SPAWNER = ENTITY_TYPES.register(createEntry(EntityZombieSpawner::new, 	"zombie_spawner", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityRadiantTotem>> RADIANT_TOTEM = ENTITY_TYPES.register(createEntry(EntityRadiantTotem::new, 	"radiant_totem", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityWitheringTotem>> WITHERING_TOTEM = ENTITY_TYPES.register(createEntry(EntityWitheringTotem::new, 	"withering_totem", MobCategory.MISC, TrackingType.CONSTRUCT));

	// These ones don't render, currently that makes no difference here but we might as well separate them
	public static final RegistryObject<EntityType<EntityArrowRain>> ARROW_RAIN = ENTITY_TYPES.register(createEntry(EntityArrowRain::new, 		"arrow_rain", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityEarthquake>> EARTHQUAKE = ENTITY_TYPES.register(createEntry(EntityEarthquake::new, 		"earthquake", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityHailstorm>> HAILSTORM = ENTITY_TYPES.register(createEntry(EntityHailstorm::new, 		"hailstorm", MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityStormcloud>> STORMCLOUD = ENTITY_TYPES.register(createEntry(EntityStormcloud::new, 		"stormcloud", MobCategory.MISC, TrackingType.CONSTRUCT));

	// These ones move, velocity updates are sent if that's not at constant velocity
	public static final RegistryObject<EntityType<EntityShield>> SHIELD = register("shield", createEntry(EntityShield::new, MobCategory.MISC, 160, 10, true).sized(1.2f, 1.4f));
	public static final RegistryObject<EntityType<EntityBubble>> BUBBLE = register("bubble", createEntry(EntityBubble::new, MobCategory.MISC, 160, 3, false));
	public static final RegistryObject<EntityType<EntityTornado>> TORNADO = register("tornado", createEntry(EntityTornado::new, MobCategory.MISC, 160, 3, false));
	public static final RegistryObject<EntityType<EntityIceSpike>> ICE_SPIKE = register("ice_spike", createEntry(EntityIceSpike::new, MobCategory.MISC, 160, 1, true));
	public static final RegistryObject<EntityType<EntityBoulder>> BOULDER = register("boulder", createEntry(EntityBoulder::new, MobCategory.MISC, 160, 1, true)); // Vertical velocity is not constant
	public static final RegistryObject<EntityType<EntityIceBarrier>> ICE_BARRIER = register("ice_barrier", createEntry(EntityIceBarrier::new, MobCategory.MISC, 160, 1, true));

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id, and accepts a {@link TrackingType} for automatic tracker assignment.
	 * @param entityClass The entity class to use.
	 * @param name The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 * 		       {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param tracking The {@link TrackingType} to use for this entity.
	 * @param <T> The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityType.Builder<T> createEntry(EntityType.EntityFactory<T> factory, MobCategory category, TrackingType tracking){
		return createEntry(factory, category).setTrackingRange(tracking.range).setUpdateInterval(tracking.interval).setShouldReceiveVelocityUpdates(tracking.trackVelocity);
	}
	
	private static <T extends Entity> EntityType.Builder<T> createEntry(EntityType.EntityFactory<T> factory, MobCategory category, int range, int interval, boolean trackVelocity){
		return createEntry(factory, category).setTrackingRange(range).setUpdateInterval(interval).setShouldReceiveVelocityUpdates(trackVelocity);
	}
	
	private static <T extends Entity> EntityType.Builder<T> createEntry(EntityType.EntityFactory<T> factory, MobCategory category){
		return EntityType.Builder.<T>of(factory, category);
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id.
	 * @param entityClass The entity class to use.
	 * @param name The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 * 		       {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param <T> The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder) {
		return ENTITY_TYPES.register(name, () -> builder.build(new ResourceLocation(Wizardry.MODID, name).toString()));
	}
}
