package electroblob.wizardry.spell;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class Firestorm extends SpellAreaEffect {

	public Firestorm(){
		super("firestorm", SpellActions.POINT_DOWN, false);
		this.soundValues(2f, 1.0f, 0);
		this.alwaysSucceed(true);
		addProperties(BURN_DURATION);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		burnNearbyBlocks(world, caster.position(), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean cast(Level world, Mob caster, InteractionHand hand, int ticksInUse, LivingEntity target, SpellModifiers modifiers){
		burnNearbyBlocks(world, caster.position(), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, target, modifiers);
	}

	@Override
	public boolean cast(Level world, double x, double y, double z, Direction direction, int ticksInUse, int duration, SpellModifiers modifiers){
		burnNearbyBlocks(world, new Vec3(x, y, z), null, modifiers);
		return super.cast(world, x, y, z, direction, ticksInUse, duration, modifiers);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){
		target.setSecondsOnFire(getProperty(BURN_DURATION).intValue());
		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){

		for(int i=0; i<100; i++){
			float r = world.random.nextFloat();
			double speed = 0.02/r * (1 + world.random.nextDouble());//(world.random.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.random.nextDouble());
			ParticleBuilder.create(Type.MAGIC_FIRE)
					.pos(origin.x, origin.y + world.random.nextDouble() * 3, origin.z)
					.vel(0, 0, 0)
					.scale(2)
					.time(40 + world.random.nextInt(10))
					.spin(world.random.nextDouble() * (radius - 0.5) + 0.5, speed)
					.spawn(world);
		}

		for(int i=0; i<60; i++){
			float r = world.random.nextFloat();
			double speed = 0.02/r * (1 + world.random.nextDouble());//(world.random.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.random.nextDouble());
			ParticleBuilder.create(Type.CLOUD)
					.pos(origin.x, origin.y + world.random.nextDouble() * 2.5, origin.z)
					.clr(DrawingUtils.mix(DrawingUtils.mix(0xffbe00, 0xff3600, r/0.6f), 0x222222, (r - 0.6f)/0.4f))
					.spin(r * (radius - 1) + 0.5, speed)
					.spawn(world);
		}
	}

	private void burnNearbyBlocks(Level world, Vec3 origin, @Nullable LivingEntity caster, SpellModifiers modifiers){

		if(!world.isClientSide && EntityUtils.canDamageBlocks(caster, world)){

			double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

			for(int i = -(int)radius; i <= (int)radius; i++){
				for(int j = -(int)radius; j <= (int)radius; j++){

					BlockPos pos = new BlockPos(origin).add(i, 0, j);

					Integer y = BlockUtils.getNearestSurface(world, new BlockPos(pos), Direction.UP, (int)radius, true, SurfaceCriteria.NOT_AIR_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = origin.distanceTo(new Vec3(origin.x + i, y, origin.z + j));

						// Randomised with weighting so that the nearer the block the more likely it is to be set alight.
						if(y != -1 && world.random.nextInt((int)(dist * 2) + 1) < radius && dist < radius && dist > 1.5
								&& BlockUtils.canPlaceBlock(caster, world, pos)){
							world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
						}
					}
				}
			}
		}
	}

}
