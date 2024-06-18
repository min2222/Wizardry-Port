package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PhaseStep extends Spell {

	public static final String WALL_THICKNESS = "wall_thickness";

	public PhaseStep(){
		super("phase_step", SpellActions.POINT, false);
		addProperties(RANGE, WALL_THICKNESS);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean teleportMount = caster.isPassenger() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_mount_teleporting);
		boolean hitLiquids = teleportMount && caster.getVehicle() instanceof Boat; // Boats teleport to the surface

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		HitResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, !hitLiquids, false);

		// This is here because the conditions are false on the client for whatever reason. (see the Javadoc for cast()
		// for an explanation)
		if(world.isClientSide){

			for(int i = 0; i < 10; i++){
				double dx1 = caster.getX();
				double dy1 = caster.getY() + 2 * world.random.nextFloat();
				double dz1 = caster.getZ();
				world.addParticle(ParticleTypes.PORTAL, dx1, dy1, dz1, world.random.nextDouble() - 0.5,
						world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5);
			}

			Wizardry.proxy.playBlinkEffect(caster);
		}

		Entity toTeleport = teleportMount ? caster.getVehicle() : caster;

		if(rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK){

			BlockPos pos = ((BlockHitResult) rayTrace).getBlockPos();

			// The maximum wall thickness as determined by the range multiplier. The + 0.5f is so that
			// weird float processing doesn't incorrectly round it down.
			int maxThickness = getProperty(WALL_THICKNESS).intValue()
					+ (int)((modifiers.get(WizardryItems.range_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);

			if(((BlockHitResult) rayTrace).getDirection() == Direction.UP) maxThickness++; // Allow space for the player's head

			// i represents how far the player needs to teleport to get through the wall
			for(int i = 0; i <= maxThickness; i++){

				BlockPos pos1 = pos.relative(((BlockHitResult) rayTrace).getDirection().getOpposite(), i);

				// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other
				// mods' mazes and dungeons.
				if((BlockUtils.isBlockUnbreakable(world, pos1) || BlockUtils.isBlockUnbreakable(world, pos1.above()))
						&& !Wizardry.settings.teleportThroughUnbreakableBlocks)
					break; // Don't return false yet, there are other possible outcomes below now

				Vec3 vec = GeometryUtils.getFaceCentre(pos1, Direction.DOWN);
				if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;
			}

			// If no suitable position was found on the other side of the wall, works like blink instead
			pos = pos.relative(((BlockHitResult) rayTrace).getDirection());

			Vec3 vec = GeometryUtils.getFaceCentre(pos, Direction.DOWN);
			if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;

		}else{ // The ray trace missed
			Vec3 vec = caster.position().add(caster.getLookAngle().scale(range));
			if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;
		}

		return false;
	}

	protected boolean attemptTeleport(Level world, Entity toTeleport, Vec3 destination, boolean teleportMount, Player caster, int ticksInUse, SpellModifiers modifiers){

		destination = EntityUtils.findSpaceForTeleport(toTeleport, destination, teleportMount);

		if(destination != null){
			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!teleportMount && caster.isPassenger()) caster.stopRiding();
			if(!world.isClientSide) toTeleport.moveTo(destination.x, destination.y, destination.z);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

}
