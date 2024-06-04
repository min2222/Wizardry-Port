package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.EntityBoat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Blink extends Spell {

	public Blink(){
		super("blink", SpellActions.POINT, false);
		addProperties(RANGE);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean teleportMount = caster.isRiding() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_mount_teleporting);
		boolean hitLiquids = teleportMount && caster.getRidingEntity() instanceof EntityBoat; // Boats teleport to the surface

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		HitResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, !hitLiquids,false);

		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.
		if(world.isClientSide){
			for(int i = 0; i < 10; i++){
				double dx = caster.getX();
				double dy = caster.getY() + 2 * world.random.nextFloat();
				double dz = caster.getZ();
				// For portal particles, velocity is not velocity but the offset where they start, then drift to
				// the actual position given.
				world.spawnParticle(ParticleTypes.PORTAL, dx, dy, dz, world.random.nextDouble() - 0.5,
						world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5);
			}

			Wizardry.proxy.playBlinkEffect(caster);
		}

		if(rayTrace != null && rayTrace.typeOfHit == HitResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);
			Entity toTeleport = teleportMount ? caster.getRidingEntity() : caster;

			Vec3 vec = EntityUtils.findSpaceForTeleport(toTeleport, GeometryUtils.getFaceCentre(pos, Direction.DOWN), teleportMount);

			if(vec != null){
				// Plays before and after so it is heard from both positions
				this.playSound(world, caster, ticksInUse, -1, modifiers);

				if(!teleportMount && caster.isRiding()) caster.dismountRidingEntity();
				if(!world.isClientSide) toTeleport.setPositionAndUpdate(vec.x, vec.y, vec.z);

				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target,
                        SpellModifiers modifiers){

		float angle = (float)(Math.atan2(target.getZ() - caster.getZ(), target.getX() - caster.getX())
				+ world.random.nextDouble() * Math.PI);
		double radius = caster.getDistance(target.getX(), target.getY(), target.getZ())
				+ world.random.nextDouble() * 3.0d;

		int x = Mth.floor(target.getX() + Mth.sin(angle) * radius);
		int z = Mth.floor(target.getZ() - Mth.cos(angle) * radius);
		Integer y = BlockUtils.getNearestFloor(world, new BlockPos(caster), (int)radius);

		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.

		// For some reason, the wizard version spwans the particles where the wizard started
		if(world.isClientSide){
			for(int i = 0; i < 10; i++){
				double dx1 = caster.getX();
				double dy1 = caster.getY() + caster.getBbHeight() * world.random.nextFloat();
				double dz1 = caster.getZ();
				world.spawnParticle(ParticleTypes.PORTAL, dx1, dy1, dz1, world.random.nextDouble() - 0.5,
						world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5);
			}
		}

		if(y != null){

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
			// not teleport 1 block above the ground.
			if(!level.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
				y--;
			}

			if(level.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
					|| level.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
				return false;
			}

			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!world.isClientSide){
				caster.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

}
