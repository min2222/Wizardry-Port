package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class IceSpikes extends SpellConstructRanged<EntityIceSpike> {
	
	public static final String ICE_SPIKE_COUNT = "ice_spike_count";

	public IceSpikes(){
		super("ice_spikes", EntityIceSpike::new, true);
		addProperties(EFFECT_RADIUS, ICE_SPIKE_COUNT, DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.ignoreUncollidables(true);
	}
	
	@Override
	protected boolean spawnConstruct(Level world, double x, double y, double z, Direction side, LivingEntity caster, SpellModifiers modifiers){

		if(side == null) return false;

		BlockPos blockHit = new BlockPos(x, y, z);
		if(side.getAxisDirection() == Direction.AxisDirection.NEGATIVE) blockHit = blockHit.offset(side);

		if(level.getBlockState(blockHit).isNormalCube()) return false;

		Vec3 origin = new Vec3(x, y, z);

		Vec3 pos = origin.add(new Vec3(side.getOpposite().getDirectionVec()));
		
		// Now always spawns a spike exactly at the position aimed at
		super.spawnConstruct(world, pos.x, pos.y, pos.z, side, caster, modifiers);
		// -1 because of the one spawned above
		int quantity = (int)(getProperty(ICE_SPIKE_COUNT).floatValue() * modifiers.get(WizardryItems.blast_upgrade)) - 1;

		float maxRadius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		for(int i=0; i<quantity; i++){

			double radius = 0.5 + world.random.nextDouble() * (maxRadius - 0.5);

			// First, generate a random vector of length radius with a z component of zero
			// Then rotate it so that what was south is now the side that was hit
			Vec3 offset = Vec3.fromPitchYaw(world.random.nextFloat() * 180 - 90, world.random.nextBoolean() ? 0 : 180)
					.scale(radius).rotateYaw(side.getHorizontalAngle() * (float)Math.PI/180).rotatePitch(GeometryUtils.getPitch(side) * (float)Math.PI/180);

			if(side.getAxis().isHorizontal()) offset = offset.rotateYaw((float)Math.PI/2);

			Integer surface = BlockUtils.getNearestSurface(world, new BlockPos(origin.add(offset)), side,
					(int)maxRadius, true, BlockUtils.SurfaceCriteria.basedOn(BlockState::isNormalCube));

			if(surface != null){
				Vec3 vec = GeometryUtils.replaceComponent(origin.add(offset), side.getAxis(), surface)
						.subtract(new Vec3(side.getDirectionVec()));
				super.spawnConstruct(world, vec.x, vec.y, vec.z, side, caster, modifiers);
			}
		}
		
		return true;
	}
	
	@Override
	protected void addConstructExtras(EntityIceSpike construct, Direction side, LivingEntity caster, SpellModifiers modifiers){
		// In this particular case, lifetime is implemented as a delay instead so is treated differently.
		construct.lifetime = 30 + construct.world.random.nextInt(15);
		construct.setFacing(side);
	}

}
