package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.EnumAction;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * Generic superclass for all spells which affect entities in a radius around the caster. This allows all
 * the relevant code to be centralised. This class differs from most other spell superclasses in that it is abstract
 * and as such must be subclassed to define what the spell actually does. This is because spells of this kind do a wider
 * variety of different things, so it does not make sense to define more specific functions in this class since they
 * would be redundant in the majority of cases.
 * <p></p>
 * <i>N.B. The abstract methods in this class have a {@link Nullable} caster parameter (the caster is null when the
 * spell is cast by a dispenser). When implementing this method, be sure to check whether the caster is {@code null}
 * and deal with it appropriately.</i>
 * <p></p>
 * Properties added by this type of spell: {@link Spell#EFFECT_RADIUS}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(Mob, boolean)}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastBy(TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#requiresPacket()}
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public abstract class SpellAreaEffect extends Spell {

	/** True if this spell should target allies of the caster instead of hostiles. Positional casting always targets
	 * everything, regardless of this setting. */
	protected boolean targetAllies = false;
	/** True if this spell should succeed even if no entities are affected, false if at least one entity must be affected. */
	protected boolean alwaysSucceed = false;
	/** The average number of particles to spawn per block in this spell's area of effect. */
	protected float particleDensity = 0.65f;
	
	public SpellAreaEffect(String name, EnumAction action, boolean continuous){
		this(Wizardry.MODID, name, action, continuous);
	}

	public SpellAreaEffect(String modID, String name, EnumAction action, boolean continuous){
		super(modID, name, action, continuous);
		this.addProperties(EFFECT_RADIUS);
		this.npcSelector((e, o) -> true);
	}
	
	/**
	 * Sets whether this spell should target allies of the caster instead of hostiles. Positional casting always targets
	 * everything, regardless of this setting.
	 * @param targetAllies True to call {@link SpellAreaEffect#affectEntity(Level, Vec3, LivingEntity, LivingEntity, int, int, SpellModifiers)}
	 *                     on allies of the caster, false to call it on entities considered hostile to the caster (not
	 *                     necessarily <i>collectively-exhaustive</i>; some entities may belong to neither category).
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellAreaEffect targetAllies(boolean targetAllies) {
		this.targetAllies = targetAllies;
		return this;
	}

	/**
	 * Sets whether this spell this spell should succeed even if no entities are affected.
	 * @param alwaysSucceed True if this spell should succeed even if no entities are affected, false if
	 *                      {@link SpellAreaEffect#affectEntity(Level, Vec3, LivingEntity, LivingEntity, int, int, SpellModifiers)}
	 *                      must return true at least once for the spell to succeed
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellAreaEffect alwaysSucceed(boolean alwaysSucceed) {
		this.alwaysSucceed = alwaysSucceed;
		return this;
	}

	/**
	 * Sets the number of particles to spawn per block for this spell.
	 * @param particleDensity The average number of particles to spawn per block in this spell's area of effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellAreaEffect particleDensity(float particleDensity) {
		this.particleDensity = particleDensity;
		return this;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return true;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		boolean result = findAndAffectEntities(world, caster.getPositionVector(),
				caster, ticksInUse, modifiers);
		if(result) this.playSound(world, caster, ticksInUse, -1, modifiers);
		return result;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		boolean result = findAndAffectEntities(world, caster.getPositionVector(), caster, ticksInUse, modifiers);
		if(result) this.playSound(world, caster, ticksInUse, -1, modifiers);
		return result;
	}

	@Override
	public boolean cast(Level world, double x, double y, double z, Direction direction, int ticksInUse, int duration, SpellModifiers modifiers){
		boolean result = findAndAffectEntities(world, new Vec3(x, y, z), null, ticksInUse, modifiers);
		if(result) this.playSound(world, x, y, z, ticksInUse, -1, modifiers);
		return result;
	}

	/** Takes care of the shared stuff for the three casting methods. This is mainly for internal use. */
	protected boolean findAndAffectEntities(Level world, Vec3 origin, @Nullable LivingEntity caster, int ticksInUse, SpellModifiers modifiers){

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);
		List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(radius, origin.x, origin.y, origin.z, world);

		if(targetAllies){
			targets.removeIf(target -> target != caster && !AllyDesignationSystem.isAllied(caster, target));
		}else{
			targets.removeIf(target -> !AllyDesignationSystem.isValidTarget(caster, target));
		}

		// Sort by distance from the origin for consistency in ordering for spells with a limit
		targets.sort(Comparator.comparingDouble(e -> e.getDistanceSq(origin.x, origin.y, origin.z)));

		boolean result = alwaysSucceed;
		int i = 0;

		for(LivingEntity target : targets){
			if(affectEntity(world, origin, caster, target, i++, ticksInUse, modifiers)) result = true;
		}

		if(world.isRemote) spawnParticleEffect(world, origin, radius, caster, modifiers);
		return result;
	}

	/**
	 * Called to do something to each entity within the spell's area of effect.
	 * @param world The world in which the spell was cast.
	 * @param origin The position the spell was cast from.
	 * @param caster The entity that cast the spell, or null if it was cast from a position.
	 * @param target The entity to do something to.
	 * @param targetCount The number of targets that have already been affected, useful for spells with a target limit.
	 *                    Targets will be called in order of distance from the caster/origin,
	 * @param ticksInUse The number of ticks the spell has already been cast for.
	 * @param modifiers The modifiers the spell was cast with.
	 * @return True if whatever was done to the entity was successful, false if not.
	 */
	protected abstract boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers);
	
	/**
	 * Called to spawn the spell's particle effect. By default, this generates a set of random points within the spell's
	 * area of effect and calls {@link SpellAreaEffect#spawnParticle(Level, double, double, double)} at each to spawn
	 * the individual particles. Only called client-side. Override to add a custom particle effect.
	 * @param world The world to spawn the particles in.
	 * @param origin The position the spell was cast from.
	 * @param radius The radius around the origin that was affected by this spell.
	 * @param caster The entity that cast the spell, or null if it was cast from a position.
	 * @param modifiers The modifiers the spell was cast with.
	 */
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){
		
		int particleCount = (int)Math.round(particleDensity * Math.PI * radius * radius);
		
		for(int i=0; i<particleCount; i++){
			
			double r = (1 + world.rand.nextDouble() * (radius - 1));
			float angle = world.rand.nextFloat() * (float)Math.PI * 2f;
			
			spawnParticle(world, origin.x + r * Mth.cos(angle), origin.y, origin.z + r * Mth.sin(angle));
		}
	}
	
	/**
	 * Called at each point within the spell's area of effect to spawn one or more particles at that point. Only called
	 * client-side. Does nothing by default.
	 * @param world The world in which to spawn the particle.
	 * @param x The x-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 * @param y The y-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 * @param z The z-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 */
	protected void spawnParticle(Level world, double x, double y, double z){}

}
