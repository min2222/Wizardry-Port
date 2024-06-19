package electroblob.wizardry.spell;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.Vec3;

public class FrostBarrier extends Spell {

	private static final double BARRIER_DISTANCE = 2;
	private static final double BARRIER_ARC_RADIUS = 10;
	private static final double BARRIER_SPACING = 1.4;

	public FrostBarrier(){
		super("frost_barrier", SpellActions.SUMMON, false);
		this.npcSelector((e, o) -> true);
		addProperties(DURATION);
	}

	@Override
	public boolean canBeCastBy(DispenserBlockEntity dispenser){
		return true;
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.isOnGround()){
			if(!createBarriers(world, caster.position(), caster.getLookAngle(), caster, modifiers)) return false;
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){

		if(caster.isOnGround()){
			if(!createBarriers(world, caster.position(), target.position().subtract(caster.position()),
					caster, modifiers)) return false;
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(Level world, double x, double y, double z, Direction direction, int ticksInUse, int duration, SpellModifiers modifiers){
		if(!createBarriers(world, new Vec3(x, y, z), new Vec3(direction.step()), null, modifiers)) return false;
		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getStepX(), y - direction.getStepY(), z - direction.getStepZ(), ticksInUse, duration, modifiers);
		return true;
	}

	private boolean createBarriers(Level world, Vec3 origin, Vec3 direction, @Nullable LivingEntity caster, SpellModifiers modifiers){

		if(!world.isClientSide){

			direction = GeometryUtils.horizontalise(direction);
			Vec3 centre = origin.add(direction.scale(BARRIER_DISTANCE - BARRIER_ARC_RADIUS)); // Arc centred behind caster

			// Don't spawn them yet or the anti-overlap will prevent the rest from spawning
			List<EntityIceBarrier> barriers = new ArrayList<>();

			int barrierCount = 1 + Math.max(1, (int)((modifiers.get(SpellModifiers.POTENCY) - 1) / Constants.POTENCY_INCREASE_PER_TIER + 0.5f));

			for(int i = 0; i < barrierCount; i++){

				EntityIceBarrier barrier = createBarrier(world, centre, direction.yRot((float)(BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
				if(barrier != null) barriers.add(barrier);

				if(i == 0) continue; // Only one in the middle
				barrier = createBarrier(world, centre, direction.yRot(-(float)(BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
				if(barrier != null) barriers.add(barrier);
			}

			if(barriers.isEmpty()) return false;

			barriers.forEach(world::addFreshEntity); // Finally spawn them all
		}

		return true;
	}

	private EntityIceBarrier createBarrier(Level world, Vec3 centre, Vec3 direction, @Nullable LivingEntity caster, SpellModifiers modifiers, int barrierCount, int index){

		Vec3 position = centre.add(direction.scale(BARRIER_ARC_RADIUS));
		Integer floor = BlockUtils.getNearestFloor(world, new BlockPos(position), 3);
		if(floor == null) return null;
		position = GeometryUtils.replaceComponent(position, Axis.Y, floor);

		float scale = 1.5f - (float)index/barrierCount * 0.5f;
		double yOffset = 1.5 * scale;

		EntityIceBarrier barrier = new EntityIceBarrier(world);
		barrier.setPos(position.x, position.y - yOffset, position.z);
		barrier.setCaster(caster);
		barrier.lifetime = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
		barrier.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
		barrier.setRot((float)Math.toDegrees(Mth.atan2(-direction.x, direction.z)), barrier.getXRot());
		barrier.setSizeMultiplier(scale);
		barrier.setDelay(1 + 3 * index); // Delay 0 seems to move it down 1 block, no idea why

		if(!world.getEntitiesOfClass(barrier.getClass(), barrier.getBoundingBox().move(0, yOffset, 0)).isEmpty()) return null;

		return barrier;
	}

}
