package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryEventHandler;

/** Stores various global constants used in Wizardry. */
public final class Constants {

	/** The amount of mana a crystal shard is worth */
	// 100 doesn't divide nicely by 9 so we're calling this 10. I guess you lose a little bit by smashing a crystal.
	public static final int MANA_PER_SHARD = 10;
	/** The amount of mana each magic crystal is worth */
	public static final int MANA_PER_CRYSTAL = 100;
	/** The amount of mana a grand magic crystal is worth */
	public static final int GRAND_CRYSTAL_MANA = 400;
	/** The maximum number of one type of wand upgrade which can be applied to a wand. */
	public static final int UPGRADE_STACK_LIMIT = 3;
	/** The bonus amount of wand upgrades that can be applied to a non-elemental wand. */
	public static final int NON_ELEMENTAL_UPGRADE_BONUS = 3;
	/** The fraction by which cooldowns are reduced for each level of cooldown upgrade. */
	public static float COOLDOWN_REDUCTION_PER_LEVEL = 0.15f;
	/** The fraction by which maximum charge is increased for each level of storage upgrade. */
	public static final float STORAGE_INCREASE_PER_LEVEL = 0.15f;
	/** The fraction by which potency is increased for each tier of matching wand. */
	public static float POTENCY_INCREASE_PER_TIER = 0.15f;
	/** The fraction by which spell duration is increased for each level of duration upgrade. */
	public static float DURATION_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell range is increased for each level of range upgrade. */
	public static float RANGE_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell blast radius is increased for each level of range upgrade. */
	public static float BLAST_RADIUS_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which movement speed is reduced per level of frost effect. */
	public static final double FROST_SLOWNESS_PER_LEVEL = 0.5;
	/** The fraction by which movement speed is reduced per level of decay effect. */
	public static final double DECAY_SLOWNESS_PER_LEVEL = 0.2;
	/** The fraction by which dig speed is reduced per level of frostbite effect. */
	public static final float FROST_FATIGUE_PER_LEVEL = 0.45f;
	/** The number of ticks between each mana increase for wands with the condenser upgrade. */
	public static final int CONDENSER_TICK_INTERVAL = 50;
	/**
	 * The amount of mana given for a kill for each level of siphon upgrade. A random amount from 0 to this number - 1
	 * is also added. See {@link WizardryEventHandler#onLivingDeathEvent} for more details.
	 */
	public static final int SIPHON_MANA_PER_LEVEL = 5;
	/**
	 * The number of ticks between the spawning of patches of decay when an entity has the decay effect. Note that decay
	 * won't spawn again if something is already standing in it.
	 */
	public static final int DECAY_SPREAD_INTERVAL = 8;


	// making this as an update to the existing values to not break addons directly relying on the fields
	static {
		POTENCY_INCREASE_PER_TIER = (float) Wizardry.settings.potencyIncreasePerTier;
		COOLDOWN_REDUCTION_PER_LEVEL = (float) Wizardry.settings.cooldownReductionPerLevel;
		DURATION_INCREASE_PER_LEVEL = (float) Wizardry.settings.durationIncreasePerLevel;
		RANGE_INCREASE_PER_LEVEL = (float) Wizardry.settings.rangeIncreasePerLevel;
		BLAST_RADIUS_INCREASE_PER_LEVEL = (float) Wizardry.settings.blastIncreasePerLevel;
		RANGE_INCREASE_PER_LEVEL = (float) Wizardry.settings.frostSlownessIncreasePerLevel;
	}
}
