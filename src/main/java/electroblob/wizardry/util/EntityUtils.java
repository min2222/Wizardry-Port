package electroblob.wizardry.util;

import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.spell.Spell;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.effect.EntityLightningBolt;
import net.minecraft.world.entity.item.EntityArmorStand;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.EntityThrowable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains useful static methods for retrieving and interacting with players, mobs and other entities. These methods
 * used to be part of {@code WizardryUtilities}.
 * @see BlockUtils
 * @see MagicDamage
 * @see AllyDesignationSystem
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class EntityUtils {

	private EntityUtils(){} // No instances!

	/** Changed to a constant in wizardry 2.1, since this is a lot more efficient. */
	private static final EntityDataSerializer<Boolean> POWERED;

	static {
		// Null is passed in deliberately since POWERED is a static field.
		POWERED = ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, null, "field_184714_b");
	}

	/** Stores constant values for attribute modifier operations (and javadoc for what they actually do!) */
	// I'm fed up with remembering these...
	public static final class Operations {

		private Operations(){} // No instances!

		/** Adds the attribute modifier amount to the base value. */
		public static final int ADD = 0;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * parallel, i.e. the calculation is based on the base value and does not depend on previous modifiers. */
		public static final int MULTIPLY_FLAT = 1;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * series, i.e. the calculation is based on the value after previous modifiers are applied, in the order added. */
		public static final int MULTIPLY_CUMULATIVE = 2;
	}

	// Entity retrieval
	// ===============================================================================================================

	/**
	 * Shorthand for {@link EntityUtils#getEntitiesWithinRadius(double, double, double, double, Level, Class)}
	 * with EntityLivingBase as the entity type. This is by far the most common use for that method.
	 *
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 */
	public static List<LivingEntity> getLivingWithinRadius(double radius, double x, double y, double z, Level world){
		return getEntitiesWithinRadius(radius, x, y, z, world, LivingEntity.class);
	}

	/**
	 * Returns all entities of the specified type within the specified radius of the given coordinates. This is
	 * different to using a raw AABB because a raw AABB will search in a cube volume rather than a sphere. Note that
	 * this does not exclude any entities; if any specific entities are to be excluded this must be checked when
	 * iterating through the list.
	 *
	 * @see EntityUtils#getLivingWithinRadius(double, double, double, double, Level)
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinRadius(double radius, double x, double y, double z, Level world, Class<T> entityType){
		AABB aabb = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
		List<T> entityList = level.getEntitiesWithinAABB(entityType, aabb);
		for(int i = 0; i < entityList.size(); i++){
			if(entityList.get(i).getDistance(x, y, z) > radius){
				entityList.remove(i);
				break;
			}
		}
		return entityList;
	}

	/**
	 * Returns all EntityLivingBase within the cylinder radius of the given coordinates. This should
	 * used by circle effects.
	 *
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param height The height of the cylinder
	 * @param world The world to search in
	 */
	public static List<LivingEntity> getLivingWithinCylinder(double radius, double x, double y, double z, double height, Level world) {
		return getEntitiesWithinCylinder(radius, x, y, z, height, world, LivingEntity.class);
	}

	/**
	 * Returns all entities of the specified type within the cylinder radius of the given coordinates. This should
	 * used by circle effects.
	 *
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param height The height of the cylinder
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinCylinder(double radius, double x, double y, double z, double height, Level world, Class<T> entityType) {
		AABB aabb = new AABB(x - radius, y, z - radius, x + radius, y + height, z + radius);
		List<T> entityList = level.getEntitiesWithinAABB(entityType, aabb);
		for(T entity : entityList) {
			if (entity.getDistance(x, entity.getY(), z) > radius) {
				entityList.remove(entity);
				break;
			}
		}
		return entityList;
	}

	/**
	 * Gets an entity from its UUID. If the UUID is known to belong to an {@code EntityPlayer}, use the more efficient
	 * {@link Level#getPlayerEntityByUUID(UUID)} instead.
	 *
	 * @param world The world the entity is in
	 * @param id The entity's UUID
	 * @return The Entity that has the given UUID, or null if no such entity exists in the specified world.
	 */
	@Nullable
	public static Entity getEntityByUUID(Level world, @Nullable UUID id){

		if(id == null) return null; // It would return null eventually but there's no point even looking

		for(Entity entity : world.loadedEntityList){
			// This is a perfect example of where you need to use .equals() and not ==. For most applications,
			// this was unnoticeable until world reload because the UUID instance or entity instance is stored.
			// Fixed now though.
			if(entity != null && entity.getUUID() != null && entity.getUUID().equals(id)){
				return entity;
			}
		}
		return null;
	}

	/**
	 * Returns the entity riding the given entity, or null if there is none. Allows for neater code now that entities
	 * have a list of passengers, because it is necessary to check that the list is not empty first.
	 */
	@Nullable
	public static Entity getRider(Entity entity){
		return !entity.getPassengers().isEmpty() ? entity.getPassengers().get(0) : null;
	}

	// Motion
	// ===============================================================================================================

	/**
	 * Undoes 1 tick's worth of velocity change due to gravity for the given entity. If the entity has no gravity,
	 * this method does nothing. This method is intended to be used in situations where entity gravity needs to be
	 * turned on and off and it is not practical to use {@link Entity#setNoGravity(boolean)}, usually if there is no
	 * easy way to get a reference to the entity to turn gravity back on.
	 *
	 * @param entity The entity to undo gravity for.
	 */
	public static void undoGravity(Entity entity){
		if(!entity.hasNoGravity()){
			double gravity = 0.04;
			if(entity instanceof EntityThrowable) gravity = 0.03;
			else if(entity instanceof Arrow) gravity = 0.05;
			else if(entity instanceof LivingEntity) gravity = 0.08;
			entity.motionY += gravity;
		}
	}

	/**
	 * Applies the standard (non-enchanted) amount of knockback to the given target, using the same calculation and
	 * strength value (0.4) as {@link LivingEntity#hurt(DamageSource, float)}. Use in conjunction with
	 * {@link EntityUtils#attackEntityWithoutKnockback(Entity, DamageSource, float)} to change the source of
	 * knockback for an attack.
	 *
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity
	 * @param target The entity to be knocked back
	 */
	public static void applyStandardKnockback(Entity attacker, LivingEntity target){
		applyStandardKnockback(attacker, target, 0.4f);
	}

	/**
	 * Applies the standard knockback calculation to the given target, using the same calculation as
	 * {@link LivingEntity#hurt(DamageSource, float)}.
	 *
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity
	 * @param target The entity to be knocked back
	 * @param strength The strength of the knockback
	 */
	public static void applyStandardKnockback(Entity attacker, LivingEntity target, float strength){
		double dx = attacker.getX() - target.getX();
		double dz;
		for(dz = attacker.getZ() - target.getZ(); dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random())
				* 0.01D){
			dx = (Math.random() - Math.random()) * 0.01D;
		}
		target.knockBack(attacker, strength, dx, dz);
	}

	/**
	 * Finds the nearest space to the specified position that the given entity can teleport to without being inside one
	 * or more solid blocks. The search volume is twice the size of the entity's bounding box (meaning that when
	 * teleported to the returned position, the original destination remains within the entity's bounding box).
	 * @param entity The entity being teleported
	 * @param destination The target position to search around
	 * @param accountForPassengers True to take passengers into account when searching for a space, false to ignore them
	 * @return The resulting position, or null if no space was found.
	 */
	public static Vec3 findSpaceForTeleport(Entity entity, Vec3 destination, boolean accountForPassengers){

		Level world = entity.world;
		AABB box = entity.getBoundingBox();

		if(accountForPassengers){
			for(Entity passenger : entity.getPassengers()){
				box = box.union(passenger.getBoundingBox());
			}
		}

		box = box.offset(destination.subtract(entity.getX(), entity.getY(), entity.getZ()));

		// All the parameters of this method are INCLUSIVE, so even the max coordinates should be rounded down
		Iterable<BlockPos> cuboid = BlockPos.getAllInBox(Mth.floor(box.minX), Mth.floor(box.minY),
				Mth.floor(box.minZ), Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ));

		if(Streams.stream(cuboid).noneMatch(b -> world.collidesWithAnyBlock(new AABB(b)))){
			// Nothing in the way
			return destination;

		}else{
			// Nearby position search
			double dx = box.maxX - box.minX;
			double dy = box.maxY - box.minY;
			double dz = box.maxZ - box.minZ;

			// Minimum space required is (nx + px) blocks * (ny + py) blocks * (nz + pz) blocks
			int nx = Mth.ceil(dx) / 2;
			int px = Mth.ceil(dx) - nx;
			int ny = Mth.ceil(dy) / 2;
			int py = Mth.ceil(dy) - ny;
			int nz = Mth.ceil(dz) / 2;
			int pz = Mth.ceil(dz) - nz;

			// Check all the blocks in and around the bounding box...
			List<BlockPos> nearby = Streams.stream(BlockPos.getAllInBox(Mth.floor(box.minX) - 1,
					Mth.floor(box.minY) - 1, Mth.floor(box.minZ) - 1,
					Mth.floor(box.maxX) + 1, Mth.floor(box.maxY) + 1,
					Mth.floor(box.maxZ) + 1)).collect(Collectors.toList());

			// ... but only return positions actually inside the box
			List<BlockPos> possiblePositions = Streams.stream(cuboid).collect(Collectors.toList());

			// Rather than iterate over each position and check if the box fits, find all solid blocks and cut out all
			// positions whose corresponding box would include them - this is waaay more efficient!
			while(!nearby.isEmpty()){

				BlockPos pos = nearby.remove(0);

				if(world.collidesWithAnyBlock(new AABB(pos))){
					Predicate<BlockPos> nearSolidBlock = b -> b.getX() >= pos.getX() - nx && b.getX() <= pos.getX() + px
														   && b.getY() >= pos.getY() - ny && b.getY() <= pos.getY() + py
														   && b.getZ() >= pos.getZ() - nz && b.getZ() <= pos.getZ() + pz;
					nearby.removeIf(nearSolidBlock);
					possiblePositions.removeIf(nearSolidBlock);
				}
			}

			if(possiblePositions.isEmpty()) return null; // No space nearby

			BlockPos nearest = possiblePositions.stream().min(Comparator.comparingDouble(b -> destination.squareDistanceTo(
					b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5))).get(); // The list can't be empty

			return GeometryUtils.getFaceCentre(nearest, Direction.DOWN);
		}
	}

	// Damage
	// ===============================================================================================================

	/**
	 * Attacks the given entity with the given damage source and amount, but preserving the entity's original velocity
	 * instead of applying knockback, as would happen with
	 * {@link LivingEntity#hurt(DamageSource, float)} <i>(More accurately, calls that method as normal
	 * and then resets the entity's velocity to what it was before).</i> Handy for when you need to damage an entity
	 * repeatedly in a short space of time.
	 *
	 * @param entity The entity to attack
	 * @param source The source of the damage
	 * @param amount The amount of damage to apply
	 * @return True if the attack succeeded, false if not.
	 */
	public static boolean attackEntityWithoutKnockback(Entity entity, DamageSource source, float amount){
		double vx = entity.motionX;
		double vy = entity.motionY;
		double vz = entity.motionZ;
		boolean succeeded = entity.hurt(source, amount);
		entity.motionX = vx;
		entity.motionY = vy;
		entity.motionZ = vz;
		return succeeded;
	}

	/**
	 * Returns whether the given {@link DamageSource} is melee damage. This method makes a best guess as to whether
	 * the damage was from a melee attack; there is no way of testing this properly.
	 * @param source The damage source to be tested.
	 * @return True if the given damage source is melee damage, false otherwise.
	 */
	public static boolean isMeleeDamage(DamageSource source){

		// With the exception of minions, melee damage always has the same entity for immediate/true source
		if(!(source instanceof MinionDamage) && source.getDirectEntity() != source.getEntity()) return false;
		if(source.isProjectile()) return false; // Projectile damage obviously isn't melee damage
		if(source.isUnblockable()) return false; // Melee damage should always be blockable
		if(!(source instanceof MinionDamage) && source instanceof IElementalDamage) return false;
		if(!(source.getEntity() instanceof LivingEntity)) return false; // Only living things can melee!

		if(source.getEntity() instanceof Player && source.getDamageLocation() != null
				&& source.getDamageLocation().distanceTo(source.getEntity().position()) > ((LivingEntity)source
				.getEntity()).getEntityAttribute(Player.REACH_DISTANCE).getAttributeValue()){
			return false; // Out of melee reach for players
		}

		// If it got through all that, chances are it's melee damage
		return true;
	}

	// Boolean checks
	// ===============================================================================================================

	/**
	 * Returns true if the given entity is an EntityLivingBase and not an armour stand; makes the code a bit neater.
	 * This was added because armour stands are a subclass of EntityLivingBase, but shouldn't necessarily be treated
	 * as living entities - this depends on the situation. <i>The given entity can safely be cast to EntityLivingBase
	 * if this method returns true.</i>
	 */
	// In my opinion, it's a bad design choice to have armour stands extend EntityLivingBase directly - it would be
	// better to make a parent class which is extended by both armour stands and EntityLivingBase and contains only
	// the code required by both.
	public static boolean isLiving(Entity entity){
		return entity instanceof LivingEntity && !(entity instanceof EntityArmorStand);
	}

	/**
	 * Checks if the given player is opped on the given server. If the server is a singleplayer or LAN server, this
	 * means they have cheats enabled.
	 */
	public static boolean isPlayerOp(Player player, MinecraftServer server){
		return server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null;
	}

	/**
	 * Checks that the given entity is allowed to damage blocks in the given world. If the entity is a player or null,
	 * this checks the player block damage config setting and (for players only) the {@code PlayerCapabilities} object,
	 * otherwise it posts a mob griefing event and returns the result.
	 * <p></p>
	 * This method only checks whether the entity can damage blocks <i>in general</i>. It can be useful in certain
	 * situations where breaking of individual blocks is out of the caller's control (e.g. explosions, which have their
	 * own events anyway) or as an optimisation where multiple blocks are affected. In most cases, however, it is
	 * advisable to use the location-specific methods in {@link BlockUtils}, as they call this method anyway and allow
	 * for better compatibility with other mods, particularly region protection mods.
	 */
	public static boolean canDamageBlocks(@Nullable Entity entity, Level world){
		if(entity == null) return Wizardry.settings.dispenserBlockDamage;
		else if(entity instanceof Player) return ((Player)entity).isAllowEdit() && Wizardry.settings.playerBlockDamage;
		return ForgeEventFactory.getMobGriefingEvent(world, entity);
	}

	/**
	 * Returns true if the given caster is currently casting the given spell by any means. This method is intended to
	 * eliminate the long and cumbersome wand use checking in event handlers, which often missed out spells cast by
	 * means other than wands.
	 * @param caster The potential spell caster, which may be a player or an {@link ISpellCaster}. Any other entity will
	 * cause this method to always return false.
	 * @param spell The spell to check for. The spell must be continuous or this method will always return false.
	 * @return True if the caster is currently casting the given spell through any means, false otherwise.
	 */
	// The reason this is a boolean check is that actually returning a spell presents a problem: players can cast two
	// continuous spells at once, one via commands and one via an item, so which do you choose? Since the main point was
	// to check for specific spells, it seems more useful to do it this way.
	public static boolean isCasting(LivingEntity caster, Spell spell){

		if(!spell.isContinuous) return false;

		if(caster instanceof Player){

			WizardData data = WizardData.get((Player)caster);

			if(data != null && data.currentlyCasting() == spell) return true;

			if(caster.isHandActive() && caster.getItemInUseMaxCount() >= spell.getChargeup()){

				ItemStack stack = caster.getItemInHand(caster.getActiveHand());

				if(stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack) == spell){
					// Don't do this, it interferes with stuff! We effectively already tested this with caster.isHandActive() anyway
//						&& ((ISpellCastingItem)stack.getItem()).canCast(stack, spell, (EntityPlayer)caster,
//						EnumHand.MAIN_HAND, 0, new SpellModifiers())){
					return true;
				}
			}

		}else if(caster instanceof ISpellCaster){
			if(((ISpellCaster)caster).getContinuousSpell() == spell) return true;
		}

		return false;
	}

	// Misc
	// ===============================================================================================================

	/** Returns the default aiming arror used by skeletons for the given difficulty. For reference, these are: Easy - 10,
	 * Normal - 6, Hard - 2, Peaceful - 10 (rarely used). */
	public static int getDefaultAimingError(Difficulty difficulty){
		switch(difficulty){
			case EASY: return 10;
			case NORMAL: return 6;
			case HARD: return 2;
			default: return 10; // Peaceful counts as easy; the only time this is used is when a player attacks a (good) wizard.
		}
	}

	/**
	 * Turns the given creeper into a charged creeper. In 1.10, this requires reflection since the DataManager keys are
	 * private. (You <i>could</i> call {@link EntityCreeper#onStruckByLightning(EntityLightningBolt)} and then heal it
	 * and extinguish it, but that's a bit awkward, and it'll trigger events and stuff...)
	 */
	// The reflection here only gets done once to initialise the POWERED field, so it's not a performance issue at all.
	public static void chargeCreeper(EntityCreeper creeper){
		creeper.getDataManager().set(POWERED, true);
	}

	// No point allowing anything other than players for these methods since other entities can use Entity#playSound.

	/**
	 * Shortcut for
	 * {@link Level#playSound(Player, double, double, double, SoundEvent, SoundSource, float, float)} where the
	 * player is null but the x, y and z coordinates are those of the passed in player. Use in preference to
	 * {@link Player#playSound(SoundEvent, float, float)} if there are client-server discrepancies.
	 */
	public static void playSoundAtPlayer(Player player, SoundEvent sound, SoundSource category, float volume,
                                         float pitch){
		player.world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, category, volume, pitch);
	}

	/**
	 * See {@link EntityUtils#playSoundAtPlayer(Player, SoundEvent, SoundSource, float, float)}. Category
	 * defaults to {@link SoundSource#PLAYERS}.
	 */
	public static void playSoundAtPlayer(Player player, SoundEvent sound, float volume, float pitch){
		player.world.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
	}

}
