package electroblob.wizardry.util;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.level.Level;

/**
 * Contains useful static methods for performing geometry operations on vectors, bounding boxes, {@code BlockPos}
 * objects, etc. These methods used to be part of {@code WizardryUtilities}.
 * @see Vec3
 * @see BlockPos
 * @see Direction
 * @see RelativeFacing
 * @see Location
 * @see RayTracer
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class GeometryUtils {

	private GeometryUtils(){} // No instances!

	/** A global offset used for placing/rendering flat things so that they appear to sit flush with the face of blocks
	 * but do not cause z-fighting at a distance where it is noticeable. */
	// This value is a compromise between flushness and minimum view distance for z-fighting to occur. 0.005 seems to
	// be immune to z-fighting until over a hundred blocks away, which is pretty good, and the distance from flat
	// surfaces is still indistinguishable.
	public static final double ANTI_Z_FIGHTING_OFFSET = 0.005;

	// Why is there a distanceSqToCentre method in Vec3i but not a getCentre method?

	/**
	 * Returns a {@link Vec3} of the coordinates at the centre of the given block position (i.e. the block coordinates
	 * plus 0.5 in x, y, and z).
	 */
	public static Vec3 getCentre(BlockPos pos){
		return new Vec3(pos).add(0.5, 0.5, 0.5);
	}

	/**
	 * Returns a {@link Vec3} of the coordinates at the centre of the given bounding box (The one in {@code AxisAlignedBB}
	 * itself is client-side only).
	 */
	public static Vec3 getCentre(AABB box){
		return new Vec3(box.minX + (box.maxX - box.minX) * 0.5, box.minY + (box.maxY - box.minY) * 0.5, box.minZ + (box.maxZ - box.minZ) * 0.5);
	}

	/**
	 * Returns a {@link Vec3} of the coordinates at the centre of the given entity's bounding box. This is more
	 * efficient than {@code GeometryUtils.getCentre(entity.getEntityBoundingBox())} as it can use the entity's fields.
	 */
	public static Vec3 getCentre(Entity entity){
		return new Vec3(entity.getX(), entity.getY() + entity.getBbHeight()/2, entity.getZ());
	}

	/**
	 * Returns a {@link Vec3} of the coordinates at the centre of the given face of the given block position (i.e. the
	 * centre of the block plus 0.5 in the given direction).
	 */
	public static Vec3 getFaceCentre(BlockPos pos, Direction face){
		return getCentre(pos).add(new Vec3(face.getDirectionVec()).scale(0.5));
	}

	/**
	 * Returns the component of the given {@link Vec3} corresponding to the given {@link Axis Axis}.
	 */
	public static double component(Vec3 vec, Axis axis){
		return new double[]{vec.x, vec.y, vec.z}[axis.ordinal()]; // Damn, that's compact.
	}

	/**
	 * Returns the component of the given {@link Vec3i} corresponding to the given {@link Axis Axis}.
	 */
	public static int component(Vec3i vec, Axis axis){
		return new int[]{vec.getX(), vec.getY(), vec.getZ()}[axis.ordinal()];
	}

	/**
	 * Returns a new {@link Vec3} with the component corresponding to the given {@link Axis Axis} replaced by the
	 * given value.
	 */
	public static Vec3 replaceComponent(Vec3 vec, Axis axis, double newValue){
		double[] components = {vec.x, vec.y, vec.z};
		components[axis.ordinal()] = newValue;
		return new Vec3(components[0], components[1], components[2]);
	}

	/**
	 * Returns a new {@link Vec3i} with the component corresponding to the given {@link Axis Axis} replaced by the
	 * given value.
	 */
	public static Vec3i replaceComponent(Vec3i vec, Axis axis, int newValue){
		int[] components = {vec.getX(), vec.getY(), vec.getZ()};
		components[axis.ordinal()] = newValue;
		return new Vec3i(components[0], components[1], components[2]);
	}

	/**
	 * Returns a normalised {@link Vec3} with the same yaw angle as the given vector, but with a y component of zero.
	 */
	public static Vec3 horizontalise(Vec3 vec){
		return replaceComponent(vec, Axis.Y, 0).normalize();
	}

	/**
	 * Returns an array of {@code Vec3d} objects representing the vertices of the given bounding box.
	 * @param box The bounding box whose vertices are to be returned.
	 * @return The list of vertices, which will contain 8 elements. Using EnumFacing initials, the order is:
	 * DNW, DNE, DSE, DSW, UNW, UNE, USE, USW. The returned coordinates are absolute (i.e. measured from the world origin).
	 */
	public static Vec3[] getVertices(AABB box){
		return new Vec3[]{
				new Vec3(box.minX, box.minY, box.minZ),
				new Vec3(box.maxX, box.minY, box.minZ),
				new Vec3(box.maxX, box.minY, box.maxZ),
				new Vec3(box.minX, box.minY, box.maxZ),
				new Vec3(box.minX, box.maxY, box.minZ),
				new Vec3(box.maxX, box.maxY, box.minZ),
				new Vec3(box.maxX, box.maxY, box.maxZ),
				new Vec3(box.minX, box.maxY, box.maxZ)
		};
	}

	/**
	 * Returns an array of {@code Vec3d} objects representing the vertices of the block at the given position.
	 * @param pos The position of the block whose vertices are to be returned.
	 * @return The list of vertices, which will contain 8 elements. Using EnumFacing initials, the order is:
	 * DNW, DNE, DSE, DSW, UNW, UNE, USE, USW. The returned coordinates are absolute (i.e. measured from the world origin).
	 */
	public static Vec3[] getVertices(Level world, BlockPos pos){
		return getVertices(world.getBlockState(pos).getBoundingBox(world, pos).offset(pos.getX(), pos.getY(), pos.getZ()));
	}

	/**
	 * Returns the pitch angle in degrees of the given {@link Direction}. For some reason {@code EnumFacing} has a get
	 * yaw method ({@link Direction#getHorizontalAngle()}) but not a get pitch method.
	 */
	public static float getPitch(Direction facing){
		return facing == Direction.UP ? 90 : facing == Direction.DOWN ? -90 : 0;
	}

}
