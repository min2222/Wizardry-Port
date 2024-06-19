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
	public static final RegistryObject<EntityType<EntityZombieMinion>> ZOMBIE_MINION = register("zombie_minion", createEntry(EntityZombieMinion::new, MobCategory.MONSTER, TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityHuskMinion>> HUSK_MINION = register("husk_minion", createEntry(EntityHuskMinion::new, MobCategory.MONSTER, TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySkeletonMinion>> SKELETON_MINION = register("skeleton_minion", createEntry(EntitySkeletonMinion::new, MobCategory.MONSTER, TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityStrayMinion>> STRAY_MINION = register(createEntry(EntityStrayMinion::new, "stray_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySpiderMinion>> SPIDER_MINION = register(createEntry(EntitySpiderMinion::new, "spider_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityBlazeMinion>> BLAZE_MINION = register(createEntry(EntityBlazeMinion::new, "blaze_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityWitherSkeletonMinion>> WITHER_SKELETON_MINION = register(createEntry(EntityWitherSkeletonMinion::new, "wither_skeleton_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntitySilverfishMinion>> SILVERFISH_MINION = register(createEntry(EntitySilverfishMinion::new, "silverfish_minion", TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityVexMinion>> VEX_MINION = register(createEntry(EntityVexMinion::new, "vex_minion", TrackingType.LIVING));

	// Custom summoned creatures
	public static final RegistryObject<EntityType<EntityIceWraith>> ICE_WRAITH = register(createEntry(EntityIceWraith::new, "ice_wraith", TrackingType.LIVING).egg(0xaafaff, 0x001ce1)
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.iceWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValues().stream()
					.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName())
							&& BiomeDictionary.hasType(b, BiomeDictionary.Type.SNOWY)
							&& !BiomeDictionary.hasType(b, BiomeDictionary.Type.FOREST))
					.collect(Collectors.toSet())));

	public static final RegistryObject<EntityType<EntityLightningWraith>> LIGHTNING_WRAITH = register(createEntry(EntityLightningWraith::new, 	"lightning_wraith", 	TrackingType.LIVING).egg(0x35424b, 0x27b9d9)
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.lightningWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValuesCollection().stream()
				.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
				.collect(Collectors.toSet())));

	public static final RegistryObject<EntityType<EntitySpiritWolf>> SPIRIT_WOLF = register(createEntry(EntitySpiritWolf::new, 		"spirit_wolf", 		TrackingType.LIVING).egg(0xbcc2e8, 0x5464c6));
	public static final RegistryObject<EntityType<EntitySpiritHorse>> SPIRIT_HORSE = register(createEntry(EntitySpiritHorse::new, 		"spirit_horse", 		TrackingType.LIVING).egg(0x5464c6, 0xbcc2e8));
	public static final RegistryObject<EntityType<EntityPhoenix>> PHOENIX = register(createEntry(EntityPhoenix::new, 			"phoenix", 			TrackingType.LIVING).egg(0xff4900, 0xfde535));
	public static final RegistryObject<EntityType<EntityIceGiant>> ICE_GIANT = register(createEntry(EntityIceGiant::new, 		"ice_giant", 			TrackingType.LIVING).egg(0x5bacd9, 0xeffaff));
	public static final RegistryObject<EntityType<EntitySpectralGolem>> SPECTRAL_GOLEM = register(createEntry(EntitySpectralGolem::new,	"spectral_golem",		TrackingType.LIVING).egg(0x5bacd9, 0xeffaff));

	public static final RegistryObject<EntityType<EntityMagicSlime>> MAGIC_SLIME = register(createEntry(EntityMagicSlime::new, 		"magic_slime", 		TrackingType.LIVING));
	public static final RegistryObject<EntityType<EntityDecoy>> DECOY = register(createEntry(EntityDecoy::new, 			"decoy", 				TrackingType.LIVING));

	// These two are only made of particles, so we can afford a lower update frequency
	public static final RegistryObject<EntityType<EntityShadowWraith>> SHADOW_WRAITH = register(createEntry(EntityShadowWraith::new, 	"shadow_wraith")		.tracker(80, 10, true).egg(0x11071c, 0x421384));
	public static final RegistryObject<EntityType<EntityStormElemental>> STORM_ELEMENTAL = register(createEntry(EntityStormElemental::new, 	"storm_elemental")	.tracker(80, 10, true).egg(0x162128, 0x135279));

	// Other living entities
	public static final RegistryObject<EntityType<EntityWizard>> WIZARD = register(createEntry(EntityWizard::new, 			"wizard", 			TrackingType.LIVING).egg(0x19295e, 0xee9312));
	public static final RegistryObject<EntityType<EntityEvilWizard>> EVIL_WIZARD = register(createEntry(EntityEvilWizard::new, 		"evil_wizard", 		TrackingType.LIVING).egg(0x290404, 0xee9312)
			// For reference: 5, 1, 1 are the parameters for the witch in vanilla
			.spawn(EnumCreatureType.MONSTER, Wizardry.settings.evilWizardSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValues().stream()
					.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
					.collect(Collectors.toSet())));
	public static final RegistryObject<EntityType<EntityRemnant>> REMNANT = register(createEntry(EntityRemnant::new, 			"remnant", 			TrackingType.LIVING).egg(0x414141, 0xe5daae));

	// Directed projectiles
	public static final RegistryObject<EntityType<EntityMagicMissile>> MAGIC_MISSILE = register(createEntry(EntityMagicMissile::new, 	"magic_missile", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceShard>> ICE_SHARD = register(createEntry(EntityIceShard::new, 		"ice_shard", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityLightningArrow>> LIGHTNING_ARROW = register(createEntry(EntityLightningArrow::new, 	"lightning_arrow", 	TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityForceArrow>> FORCE_ARROW = register(createEntry(EntityForceArrow::new, 		"force_arrow", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityDart>> DART = register(createEntry(EntityDart::new, 			"dart", 				TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceLance>> ICE_LANCE = register(createEntry(EntityIceLance::new, 		"ice_lance", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityFlamecatcherArrow>> FLAMECATCHER_ARROW = register(createEntry(EntityFlamecatcherArrow::new, "flamecatcher_arrow", TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityConjuredArrow>> CONJURED_ARROW = register(createEntry(EntityConjuredArrow::new, "conjured_arrow", TrackingType.PROJECTILE));

	// Directionless projectiles
	public static final RegistryObject<EntityType<EntityFirebomb>> FIREBOMB = register(createEntry(EntityFirebomb::new, 		"firebomb", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityPoisonBomb>> POISON_BOMB = register(createEntry(EntityPoisonBomb::new, 		"poison_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySparkBomb>> SPARK_BOMB = register(createEntry(EntitySparkBomb::new, 		"spark_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySmokeBomb>> SMOKE_BOMB = register(createEntry(EntitySmokeBomb::new, 		"smoke_bomb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityIceCharge>> ICE_CHARGE = register(createEntry(EntityIceCharge::new, 		"ice_charge", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityForceOrb>> FORCE_ORB = register(createEntry(EntityForceOrb::new, 		"force_orb", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntitySpark>> SPARK = register(createEntry(EntitySpark::new, 			"spark", 				TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityDarknessOrb>> DARKNESS_ORB = register(createEntry(EntityDarknessOrb::new, 		"darkness_orb", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityFirebolt>> FIREBOLT = register(createEntry(EntityFirebolt::new, 		"firebolt", 			TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityThunderbolt>> THUNDERBOLT = register(createEntry(EntityThunderbolt::new, 		"thunderbolt", 		TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityLightningDisc>> LIGHTNING_DISC = register(createEntry(EntityLightningDisc::new, 	"lightning_disc", 	TrackingType.PROJECTILE));
	public static final RegistryObject<EntityType<EntityEmber>> EMBER = register("ember", createEntry(EntityEmber::new, MobCategory.MISC, TrackingType.PROJECTILE).sized(0.1f, 0.1f));
	public static final RegistryObject<EntityType<EntityMagicFireball>> MAGIC_FIREBALL = register("magic_fireball", createEntry(EntityMagicFireball::new, MobCategory.MISC, TrackingType.PROJECTILE).sized(0.5f, 0.5f));
	public static final RegistryObject<EntityType<EntityLargeMagicFireball>> LARGE_MAGIC_FIREBALL = register("large_magic_fireball", createEntry(EntityLargeMagicFireball::new, MobCategory.MISC, TrackingType.PROJECTILE).sized(1, 1));
	public static final RegistryObject<EntityType<EntityIceball>> ICEBALL = register("iceball", createEntry(EntityIceball::new, MobCategory.MISC, TrackingType.PROJECTILE).sized(0.5f, 0.5f));

	// These are effectively projectiles, but since they're bigger and start high up they need updating from further away
	public static final RegistryObject<EntityType<EntityMeteor>> METEOR = register("meteor", createEntry(EntityMeteor::new, MobCategory.MISC, 160, 3, true).sized(0.98F, 0.98F));
	public static final RegistryObject<EntityType<EntityHammer>> LIGHTNING_HAMMER = register("lightning_hammer", createEntry(EntityHammer::new, MobCategory.MISC, 160, 3, true));
	public static final RegistryObject<EntityType<EntityLevitatingBlock>> LEVITATING_BLOCK = register("levitating_block", createEntry(EntityLevitatingBlock::new, MobCategory.MISC, 160, 3, true).sized(0.98F, 0.98F));

	// Constructs
	public static final RegistryObject<EntityType<EntityBlackHole>> BLACK_HOLE = register("black_hole", createEntry(EntityBlackHole::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityBlizzard>> BLIZZARD = register("blizzard", createEntry(EntityBlizzard::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityForcefield>> FORCEFIELD = register("forcefield", createEntry(EntityForcefield::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFireSigil>> FIRE_SIGIL = register("fire_sigil", createEntry(EntityFireSigil::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFrostSigil>> FROST_SIGIL = register("frost_sigil", createEntry(EntityFrostSigil::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityLightningSigil>> LIGHTNING_SIGIL = register("lightning_sigil", createEntry(EntityLightningSigil::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityCombustionRune>> COMBUSTION_RUNE = register("combustion_rune", createEntry(EntityCombustionRune::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityFireRing>> RING_OF_FIRE = register("ring_of_fire", createEntry(EntityFireRing::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityHealAura>> HEALING_AURA = register("healing_aura", createEntry(EntityHealAura::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityDecay>> DECAY = register("decay", createEntry(EntityDecay::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityZombieSpawner>> ZOMBIE_SPAWNER = register("zombie_spawner", createEntry(EntityZombieSpawner::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityRadiantTotem>> RADIANT_TOTEM = register("radiant_totem", createEntry(EntityRadiantTotem::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityWitheringTotem>> WITHERING_TOTEM = register("withering_totem", createEntry(EntityWitheringTotem::new, MobCategory.MISC, TrackingType.CONSTRUCT));

	// These ones don't render, currently that makes no difference here but we might as well separate them
	public static final RegistryObject<EntityType<EntityArrowRain>> ARROW_RAIN = register("arrow_rain", createEntry(EntityArrowRain::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityEarthquake>> EARTHQUAKE = register("earthquake", createEntry(EntityEarthquake::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityHailstorm>> HAILSTORM = register("hailstorm", createEntry(EntityHailstorm::new, MobCategory.MISC, TrackingType.CONSTRUCT));
	public static final RegistryObject<EntityType<EntityStormcloud>> STORMCLOUD = register("stormcloud", createEntry(EntityStormcloud::new, MobCategory.MISC, TrackingType.CONSTRUCT));

	// These ones move, velocity updates are sent if that's not at constant velocity
	public static final RegistryObject<EntityType<EntityShield>> SHIELD = register("shield", createEntry(EntityShield::new, MobCategory.MISC, 160, 10, true).sized(1.2f, 1.4f));
	public static final RegistryObject<EntityType<EntityBubble>> BUBBLE = register("bubble", createEntry(EntityBubble::new, MobCategory.MISC, 160, 3, false));
	public static final RegistryObject<EntityType<EntityTornado>> TORNADO = register("tornado", createEntry(EntityTornado::new, MobCategory.MISC, 160, 3, false));
	public static final RegistryObject<EntityType<EntityIceSpike>> ICE_SPIKE = register("ice_spike", createEntry(EntityIceSpike::new, MobCategory.MISC, 160, 1, true));
	public static final RegistryObject<EntityType<EntityBoulder>> BOULDER = register("boulder", createEntry(EntityBoulder::new, MobCategory.MISC, 160, 1, true)); // Vertical velocity is not constant
	public static final RegistryObject<EntityType<EntityIceBarrier>> ICE_BARRIER = register("ice_barrier", createEntry(EntityIceBarrier::new, MobCategory.MISC, 160, 1, true).sized(1.8f, 1.05f));

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
