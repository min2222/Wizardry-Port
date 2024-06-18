package electroblob.wizardry.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import electroblob.wizardry.entity.ICustomHitbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Contains a number of static methods that perform raytracing and related functions. This was split off from
 * {@code WizardryUtilities} as of wizardry 4.2 in an effort to make the code easier to navigate.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
public final class RayTracer {

	private RayTracer(){} // No instances!

	/**
	 * Helper method which performs a ray trace for <b>blocks only</b> from an entity's eye position in the direction
	 * they are looking, over a specified range, using {@link Level#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)}.
	 *
	 * @param world The world in which to perform the ray trace.
	 * @param entity The entity from which to perform the ray trace. The ray trace will start from this entity's eye
	 * position and proceed in the direction the entity is looking.
	 * @param range The distance over which the ray trace will be performed.
	 * @param hitLiquids True to return hits on the surfaces of liquids, false to ignore liquid blocks as if they were
	 * not there. {@code ignoreUncollidables} must be set to false for this setting to have an effect.
	 * @param ignoreUncollidables Whether blocks with no collisions should be ignored
	 * @param returnLastUncollidable If blocks with no collisions are ignored, whether to return the last one (useful if,
	 *                               for example, you want to replace snow layers or tall grass)
	 * @return A {@link HitResult} representing the object that was hit, which may be either a block or nothing.
	 * Returns {@code null} only if the origin and endpoint are within the same block.
	 */
	@Nullable
	public static HitResult standardBlockRayTrace(Level world, LivingEntity entity, double range, boolean hitLiquids,
                                                  boolean ignoreUncollidables, boolean returnLastUncollidable){
		// This method does not apply an offset like ray spells do, since it is not desirable in most other use cases.
		Vec3 origin = entity.getEyePosition(1);
		Vec3 endpoint = origin.add(entity.getLookAngle().scale(range));
		return world.clip(new ClipContext(origin, endpoint, ClipContext.Block.COLLIDER, hitLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, entity));
	}

	/**
	 * Helper method which performs a ray trace for <b>blocks only</b> from an entity's eye position in the direction
	 * they are looking, over a specified range. This is a shorthand for
	 * {@link #standardBlockRayTrace(Level, LivingEntity, double, boolean, boolean, boolean)}; ignoreUncollidables
	 * and returnLastUncollidable default to false.
	 */
	@Nullable
	public static HitResult standardBlockRayTrace(Level world, LivingEntity entity, double range, boolean hitLiquids){
		return standardBlockRayTrace(world, entity, range, hitLiquids, false, false);
	}

	/**
	 * Helper method which performs a ray trace for blocks and entities from an entity's eye position in the direction
	 * they are looking, over a specified range, using {@link RayTracer#rayTrace(Level, Vec3, Vec3, float, boolean, boolean, boolean, Class, Predicate)}. Aim assist is zero, the entity type is simply {@code Entity} (all entities), and the
	 * filter removes the given entity and any dying entities and allows all others.
	 *
	 * @param world The world in which to perform the ray trace.
	 * @param entity The entity from which to perform the ray trace. The ray trace will start from this entity's eye
	 * position and proceed in the direction the entity is looking. This entity will be ignored when ray tracing.
	 * @param range The distance over which the ray trace will be performed.
	 * @param hitLiquids True to return hits on the surfaces of liquids, false to ignore liquid blocks as if they were
	 * not there. {@code ignoreUncollidables} must be set to false for this setting to have an effect.
	 * @return A {@link HitResult} representing the object that was hit, which may be an entity, a block or
	 * nothing. Returns {@code null} only if the origin and endpoint are within the same block and no entity was hit.
	 */
	@Nullable
	public static HitResult standardEntityRayTrace(Level world, Entity entity, double range, boolean hitLiquids){
		// This method does not apply an offset like ray spells do, since it is not desirable in most other use cases.
		Vec3 origin = entity.getEyePosition(1);
		Vec3 endpoint = origin.add(entity.getLookAngle().scale(range));
		return rayTrace(world, origin, endpoint, 0, hitLiquids, false, false, Entity.class, ignoreEntityFilter(entity));
	}

	/**
	 * Helper method for use with {@link RayTracer#rayTrace(Level, Vec3, Vec3, float, boolean, boolean, boolean, Class, Predicate)}
	 * which returns a {@link Predicate} that returns true for the given entity, plus any entities that are in the
	 * process of dying. This is a commonly used filter in spells.
	 *
	 * @param entity The entity that the returned predicate should return true for.
	 * @return A {@link Predicate} that returns true for the given entity and any entities that are in the process of
	 * dying, false for all other entities.
	 */
	public static Predicate<Entity> ignoreEntityFilter(Entity entity){
		// Use deathTime > 0 so we still hit stuff that has *just* died, otherwise SpellRay#onEntityHit doesn't get
		// called on the client side when the entity is killed
		return e -> e == entity || (e instanceof LivingEntity && ((LivingEntity)e).deathTime > 0);
	}

	/**
	 * Performs a ray trace for blocks and entities, starting at the given origin and finishing at the given endpoint.
	 * As of wizardry 4.2, the ray tracing methods have been rewritten to be more user-friendly and implement proper
	 * aim assist.
	 * <p></p>
	 * <i>N.B. It is possible to ignore entities entirely by passing in a {@code Predicate} that is always false;
	 * however, in this specific case it is more efficient to use
	 * {@link Level#rayTraceBlocks(Vec3, Vec3, boolean, boolean, boolean)} or one of its overloads.</i>
	 *
	 * @param world The world in which to perform the ray trace.
	 * @param origin A vector representing the coordinates of the start point of the ray trace.
	 * @param endpoint A vector representing the coordinates of the finish point of the ray trace.
	 * @param aimAssist In addition to direct hits, the ray trace will also hit entities that are up to this distance
	 * from its path. For a normal ray trace, this should be 0. Values greater than 0 will give an 'aim assist' effect.
	 * @param hitLiquids True to return hits on the surfaces of liquids, false to ignore liquid blocks as if they were
	 * not there. {@code ignoreUncollidables} must be set to false for this setting to have an effect.
	 * @param ignoreUncollidables Whether blocks with no collisions should be ignored
	 * @param returnLastUncollidable If blocks with no collisions are ignored, whether to return the last one (useful if,
	 *                               for example, you want to replace snow layers or tall grass)
	 * @param entityType The class of entities to include; all other entities will be ignored.
	 * @param filter A {@link Predicate} which filters out entities that can be ignored; often used to exclude the
	 * player that is performing the ray trace.
	 *
	 * @return A {@link HitResult} representing the object that was hit, which may be an entity, a block or
	 * nothing. Returns {@code null} only if the origin and endpoint are within the same block and no entity was hit.
	 *
	 * @see RayTracer#standardEntityRayTrace(Level, Entity, double, boolean)
	 * @see RayTracer#standardBlockRayTrace(Level, LivingEntity, double, boolean)
	 */
	// Interestingly enough, aimAssist can be negative, which means hits have to be in the middle of entities!
	@Nullable
	public static HitResult rayTrace(Level world, Vec3 origin, Vec3 endpoint, float aimAssist,
                                     boolean hitLiquids, boolean ignoreUncollidables, boolean returnLastUncollidable, Class<? extends Entity> entityType, Predicate<? super Entity> filter){

		// 1 is the standard amount of extra search volume, and aim assist needs to increase this further as well as
		// expanding the entities' bounding boxes.
		float borderSize = 1 + aimAssist;

		// The AxisAlignedBB constructor accepts min/max coords in either order.
		AABB searchVolume = new AABB(origin.x, origin.y, origin.z, endpoint.x, endpoint.y, endpoint.z)
				.inflate(borderSize, borderSize, borderSize);

		// Gets all of the entities in the bounding box that could be collided with.
		List<? extends Entity> entities = world.getEntitiesOfClass(entityType, searchVolume);
		// Applies the given filter to remove entities that should be ignored.
		entities.removeIf(filter);

		// Finds the first block hit by the ray trace, if any.
		HitResult result = world.clip(new ClipContext(origin, endpoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));

		// Clips the entity search range to the part of the ray trace before the block hit, if it hit a block.
		if(result != null){
			endpoint = result.getLocation();
		}

		// Search variables
		Entity closestHitEntity = null;
		Vec3 closestHitPosition = endpoint;
		AABB entityBounds;
		Vec3 intercept = null;

		// Iterates through all the entities
		for(Entity entity : entities){

			// I'd like to add the following line so we can, for example, use greater telekinesis through a
			// ring of fire, but doing so will stop forcefields blocking particles
			//if(!entity.canBeCollidedWith()) continue;

			float fuzziness = EntityUtils.isLiving(entity) ? aimAssist : 0; // Only living entities have aim assist

			if(entity instanceof ICustomHitbox){ // Custom hitboxes
				intercept = ((ICustomHitbox)entity).calculateIntercept(origin, endpoint, fuzziness);

			}else{ // Normal hit detection

				entityBounds = entity.getBoundingBox();

				if(entityBounds != null){

					// This is zero for everything except fireballs...
					float entityBorderSize = entity.getPickRadius();
					// ... meaning the following line does nothing in all other cases.
					// -> Added the non-zero check to prevent unnecessary AABB object creation.
					if(entityBorderSize != 0)
						entityBounds = entityBounds.inflate(entityBorderSize, entityBorderSize, entityBorderSize);

					// Aim assist expands the bounding box to hit entities within the specified distance of the ray trace.
					if(fuzziness != 0) entityBounds = entityBounds.inflate(fuzziness, fuzziness, fuzziness);

					// Finds the first point at which the ray trace intercepts the entity's bounding box, if any.
                    Optional<Vec3> hit = entityBounds.clip(origin, endpoint);
                    if (hit.isPresent()) {
                        intercept = hit.get();
                    }
				}
			}

			// If the ray trace hit the entity...
			if(intercept != null){
				// Decides whether the entity that was hit is the closest so far, and if so, overwrites the old one.
				float currentHitDistance = (float)intercept.distanceTo(origin);
				float closestHitDistance = (float)closestHitPosition.distanceTo(origin);
				if(currentHitDistance < closestHitDistance){
					closestHitEntity = entity;
					closestHitPosition = intercept;
				}
			}
		}

		// If the ray trace hit an entity, return that entity; otherwise return the result of the block ray trace.
		if(closestHitEntity != null){
			result = new EntityHitResult(closestHitEntity, closestHitPosition);
		}

		return result;
	}
}
