package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class WallOfFrost extends SpellRay {

	private static final int MINIMUM_PLACEMENT_RANGE = 2;
	
	public WallOfFrost(){
		super("wall_of_frost", SpellActions.POINT, true);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
		addProperties(DURATION);
		soundValues(0.5f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(Level world, LivingEntity entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(Level world, Entity target, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){
		// Wall of frost now freezes entities solid too!
		if(target instanceof Mob && !world.isClientSide){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((Mob)target,
					caster, (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
				
				target.playSound(WizardrySounds.MISC_FREEZE, 1.0F, world.random.nextFloat() * 0.4F + 0.8F);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(Level world, BlockPos pos, Direction side, Vec3 hit, LivingEntity caster, Vec3 origin, int ticksInUse, SpellModifiers modifiers){

		if(!world.isClientSide && EntityUtils.canDamageBlocks(caster, world)){

			// Stops the ice being placed floating above snow and grass. Directions other than up included for
			// completeness.
			if(BlockUtils.canBlockBeReplaced(world, pos)){
				// Moves the blockpos back into the block
				pos = pos.relative(side.getOpposite());
			}

			if(origin.squareDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > MINIMUM_PLACEMENT_RANGE * MINIMUM_PLACEMENT_RANGE
					&& level.getBlockState(pos).getBlock() != WizardryBlocks.ice_statue && level.getBlockState(pos).getBlock() != WizardryBlocks.dry_frosted_ice){

				pos = pos.relative(side);
				
				int duration = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));

				if(BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canPlaceBlock(caster, world, pos)){
					world.setBlockAndUpdate(pos, WizardryBlocks.dry_frosted_ice.defaultBlockState());
					world.scheduleUpdate(pos.toImmutable(), WizardryBlocks.dry_frosted_ice, duration);
				}

				// Builds a 2 block high wall if it hits the ground
				if(side == Direction.UP){
					pos = pos.relative(side);

					if(BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canPlaceBlock(caster, world, pos)){
						world.setBlockAndUpdate(pos, WizardryBlocks.dry_frosted_ice.defaultBlockState());
						world.scheduleUpdate(pos.toImmutable(), WizardryBlocks.dry_frosted_ice, duration);
					}
				}
			}
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(Level world, LivingEntity caster, Vec3 origin, Vec3 direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(Level world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.random.nextFloat();
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.random.nextInt(12))
		.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.random.nextInt(12)).spawn(world);
	}

}
