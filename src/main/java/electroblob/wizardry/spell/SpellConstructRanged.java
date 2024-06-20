package electroblob.wizardry.spell;

import java.util.function.Function;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Generic superclass for all spells which conjure constructs (i.e. instances of {@link EntityMagicConstruct}) at an
 * aimed-at position (players and dispensers) or target (non-player spell casters).
 * This allows all the relevant code to be centralised, since these spells all work in a similar way. Usually, a simple
 * instantiation of this class is sufficient to create a construct spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the construct entity class instead whenever possible.
 * <p></p>
 * Properties added by this type of spell: {@link Spell#RANGE}, {@link Spell#DURATION} (if the construct is not
 * permanent)
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(Mob, boolean)}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastBy(DispenserBlockEntity)}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellConstruct
 */
public class SpellConstructRanged<T extends EntityMagicConstruct> extends SpellConstruct<T> {

	/** Whether liquids count as blocks when raytracing. Defaults to false. */
	protected boolean hitLiquids = false;
	/** Whether to ignore uncollidable blocks when raytracing. Defaults to false. */
	protected boolean ignoreUncollidables = false;

	public SpellConstructRanged(String name, Function<Level, T> constructFactory, boolean permanent){
		this(Wizardry.MODID, name, constructFactory, permanent);
	}

	public SpellConstructRanged(String modID, String name, Function<Level, T> constructFactory, boolean permanent){
		super(modID, name, SpellActions.POINT, constructFactory, permanent);
		this.addProperties(RANGE);
		this.npcSelector((e, o) -> true);
	}

	/**
	 * Sets whether liquids count as blocks when raytracing.
	 * @param hitLiquids Whether to hit liquids when raytracing. If this is false, the spell will pass through
	 * liquids as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell hitLiquids(boolean hitLiquids){
		this.hitLiquids = hitLiquids;
		return this;
	}

	/**
	 * Sets whether uncollidable blocks are ignored when raytracing.
	 * @param ignoreUncollidables Whether to hit uncollidable blocks when raytracing. If this is false, the spell will
	 * pass through uncollidable blocks as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell ignoreUncollidables(boolean ignoreUncollidables){
		this.ignoreUncollidables = ignoreUncollidables;
		return this;
	}

	@Override public boolean requiresPacket(){ return false; }
	
	@Override public boolean canBeCastBy(DispenserBlockEntity dispenser) { return true; }
	
	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());
		HitResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, ignoreUncollidables, false);

		if(rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK && (((BlockHitResult) rayTrace).getDirection() == Direction.UP ||
				!requiresFloor)){
			
			if(!world.isClientSide){
				
				double x = rayTrace.getLocation().x;
				double y = rayTrace.getLocation().y;
				double z = rayTrace.getLocation().z;
				
				if(!spawnConstruct(world, x, y, z, ((BlockHitResult) rayTrace).getDirection(), caster, modifiers)) return false;
			}
			
		}else if(!requiresFloor){
			
			if(!world.isClientSide){
				
				Vec3 look = caster.getLookAngle();
				
				double x = caster.getX() + look.x * range;
				double y = caster.getY() + caster.getEyeHeight() + look.y * range;
				double z = caster.getZ() + look.z * range;
				
				if(!spawnConstruct(world, x, y, z, null, caster, modifiers)) return false;
			}
			
		}else{
			return false;
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());
		Vec3 origin = caster.getEyePosition(1);

		if(target != null && caster.distanceTo(target) <= range){

			if(!world.isClientSide){
				
				double x = target.getX();
				double y = target.getY();
				double z = target.getZ();
				
				HitResult hit = world.clip(new ClipContext(origin, new Vec3(x, y, z), ClipContext.Block.COLLIDER, hitLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, null));

				if(hit != null && hit.getType() == HitResult.Type.BLOCK && !((BlockHitResult) hit).getBlockPos().equals(new BlockPos(x, y, z))){
					return false; // Something was in the way
				}

				Direction side = null;
				
				// If the target is not on the ground but the construct must be placed on the floor, searches for the
				// floor under the caster and returns false if it does not find one within 3 blocks.
				if(!target.isOnGround() && requiresFloor){
					Integer floor = BlockUtils.getNearestFloor(world, new BlockPos(x, y, z), 3);
					if(floor == null) return false;
					y = floor;
					side = Direction.UP;
				}
				
				if(!spawnConstruct(world, x, y, z, side, caster, modifiers)) return false;
			}
			
			caster.swing(hand);
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}
	
	@Override
	public boolean cast(Level world, double x, double y, double z, Direction direction, int ticksInUse, int duration, SpellModifiers modifiers){
		
		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());
		Vec3 origin = new Vec3(x, y, z);
		Vec3 endpoint = origin.add(new Vec3(direction.step()).scale(range));
        HitResult rayTrace = world.clip(new ClipContext(origin, endpoint, ClipContext.Block.COLLIDER, hitLiquids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, null));

		if(rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK && (((BlockHitResult) rayTrace).getDirection() == Direction.UP ||
				!requiresFloor)){
			
			if(!world.isClientSide){
				
				double x1 = rayTrace.getLocation().x;
				double y1 = rayTrace.getLocation().y;
				double z1 = rayTrace.getLocation().z;
				
				if(!spawnConstruct(world, x1, y1, z1, ((BlockHitResult) rayTrace).getDirection(), null, modifiers)) return false;
			}
			
		}else if(!requiresFloor){
			
			if(!world.isClientSide){
				
				if(!spawnConstruct(world, endpoint.x, endpoint.y, endpoint.z, null, null, modifiers)) return false;
			}
			
		}else{
			return false;
		}

		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getStepX(), y - direction.getStepY(), z - direction.getStepZ(), ticksInUse, duration, modifiers);
		return true;
	}

}
