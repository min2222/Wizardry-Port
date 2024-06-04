package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.ITickable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SpeedTime extends Spell {

	/** The base number of ticks to add to the world time for each tick the spell is cast. */
	public static final String TIME_INCREMENT = "time_increment";
	/** The number of extra times to tick each nearby block, entity and tile entity each tick the spell is cast. */
	public static final String EXTRA_TICKS = "extra_ticks";

	public SpeedTime(){
		super("speed_time", SpellActions.POINT_UP, true);
		addProperties(EFFECT_RADIUS, TIME_INCREMENT, EXTRA_TICKS);
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
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		// Hold onto your hats ladies and gentlemen, this effect scales with potency modifiers! Speeeeeeeeed!
		if(Wizardry.settings.worldTimeManipulation){
			world.setWorldTime(world.getWorldTime() + (long)(getProperty(TIME_INCREMENT).floatValue() * modifiers.get(SpellModifiers.POTENCY)));
			flag = true;
		}

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		// Doubles the normal effect of the modifier
		float potencyLevel = ((modifiers.get(SpellModifiers.POTENCY) - 1) * 2 + 1) * getProperty(EXTRA_TICKS).floatValue();

		// Ticks all the entities near the caster
		List<Entity> entities = new ArrayList<>(world.loadedEntityList);
		entities.removeIf(e -> e instanceof Player);
		entities.removeIf(e -> caster.getDistance(e) > radius);

		if(!entities.isEmpty()){
			for(int i = 0; i < potencyLevel; i++){
				entities.forEach(Entity::tick);
			}
			flag = true;
		}

		// Ticks all the tile entities near the caster
		// Copy the list first!
		List<BlockEntity> tileentities = new ArrayList<>(world.tickableTileEntities);
		tileentities.removeIf(t -> caster.distanceToSqr(t.getPos()) > radius*radius);

		if(!tileentities.isEmpty()){
			for(int i = 0; i < potencyLevel; i++){
				tileentities.forEach(t -> ((ITickable)t).update());
			}
			flag = true;
		}

		if(!world.isClientSide){

			List<BlockPos> sphere = BlockUtils.getBlockSphere(caster.getPosition(), radius);

			for(BlockPos pos : sphere){

				if(world.getBlockState(pos).getBlock().getTickRandomly()){
					for(int i = 0; i < potencyLevel; i++){
						world.getBlockState(pos).getBlock().randomTick(world, pos, world.getBlockState(pos), world.rand);
						flag = true;
					}
				}
			}
		}

		// Particle effects
		if(world.isClientSide){

			for(int i=1; i<3; i++){

				double particleSpread = 2;
				double x = caster.getX() + 2;
				double y = caster.getY() + caster.getBbHeight() / 2;
				double z = caster.getZ();

				ParticleBuilder.create(ParticleBuilder.Type.SPARKLE, world.rand, x, y, z, particleSpread, false)
						.vel(-0.25, 0, 0).time(16).clr(1f, 1f, 1f).spawn(world);

				ParticleBuilder.create(ParticleBuilder.Type.FLASH, world.rand, x, y, z, particleSpread, false)
						.vel(-0.25, 0, 0).time(16).scale(0.5f).clr(0.6f + world.random.nextFloat() * 0.4f,
						0.6f + world.random.nextFloat() * 0.4f, 0.6f + world.random.nextFloat() * 0.4f).spawn(world);
			}
		}

		if(flag) playSound(world, caster, ticksInUse, -1, modifiers);
		// Always return true if the world time was changed, otherwise return false if nothing was ticked.
		return flag;
	}

}
