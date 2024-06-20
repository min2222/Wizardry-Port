package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Blink extends Spell {

	public Blink(){
		super("blink", SpellActions.POINT, false);
		addProperties(RANGE);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean teleportMount = caster.isPassenger() && ItemArtefact.isArtefactActive(caster, WizardryItems.CHARM_MOUNT_TELEPORTING.get());
		boolean hitLiquids = teleportMount && caster.getVehicle() instanceof Boat; // Boats teleport to the surface

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.RANGE_UPGRADE.get());

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
				world.addParticle(ParticleTypes.PORTAL, dx, dy, dz, world.random.nextDouble() - 0.5,
						world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5);
			}

			Wizardry.proxy.playBlinkEffect(caster);
		}

		if(rayTrace != null && rayTrace.getType() == HitResult.Type.BLOCK){

			BlockPos pos = ((BlockHitResult) rayTrace).getBlockPos().relative(((BlockHitResult) rayTrace).getDirection());
			Entity toTeleport = teleportMount ? caster.getVehicle() : caster;

			Vec3 vec = EntityUtils.findSpaceForTeleport(toTeleport, GeometryUtils.getFaceCentre(pos, Direction.DOWN), teleportMount);

			if(vec != null){
				// Plays before and after so it is heard from both positions
				this.playSound(world, caster, ticksInUse, -1, modifiers);

				if(!teleportMount && caster.isPassenger()) caster.stopRiding();
				if(!world.isClientSide) toTeleport.moveTo(vec.x, vec.y, vec.z);

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
		double radius = caster.distanceToSqr(target.getX(), target.getY(), target.getZ())
				+ world.random.nextDouble() * 3.0d;

		int x = Mth.floor(target.getX() + Mth.sin(angle) * radius);
		int z = Mth.floor(target.getZ() - Mth.cos(angle) * radius);
		Integer y = BlockUtils.getNearestFloor(world, caster.blockPosition(), (int)radius);

		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.

		// For some reason, the wizard version spwans the particles where the wizard started
		if(world.isClientSide){
			for(int i = 0; i < 10; i++){
				double dx1 = caster.getX();
				double dy1 = caster.getY() + caster.getBbHeight() * world.random.nextFloat();
				double dz1 = caster.getZ();
				world.addParticle(ParticleTypes.PORTAL, dx1, dy1, dz1, world.random.nextDouble() - 0.5,
						world.random.nextDouble() - 0.5, world.random.nextDouble() - 0.5);
			}
		}

		if(y != null){

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
			// not teleport 1 block above the ground.
			if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMotion()){
				y--;
			}

			if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMotion()
					|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMotion()){
				return false;
			}

			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!world.isClientSide){
				caster.moveTo(x + 0.5, y + 1, z + 0.5);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swing(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(Mob npc, boolean override){
		return true;
	}

}
