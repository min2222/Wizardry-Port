package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Flight extends Spell {

	public static final String SPEED = "speed";
	public static final String ACCELERATION = "acceleration";

	private static final double Y_NUDGE_ACCELERATION = 0.075;

	public Flight(){
		super("flight", SpellActions.POINT, true);
		addProperties(SPEED, ACCELERATION);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!caster.isInWater() && !caster.isInLava() && !caster.isFallFlying()){

			float speed = getProperty(SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);
			float acceleration = getProperty(ACCELERATION).floatValue() * modifiers.get(SpellModifiers.POTENCY);

			// The division thingy checks if the look direction is the opposite way to the velocity. If this is the
			// case then the velocity should be added regardless of the player's current speed.
			if((Math.abs(caster.getDeltaMovement().x) < speed || caster.getDeltaMovement().x / caster.getLookAngle().x < 0)
					&& (Math.abs(caster.getDeltaMovement().z) < speed || caster.getDeltaMovement().z / caster.getLookAngle().z < 0)){
				caster.push(caster.getLookAngle().x * acceleration, 0, caster.getLookAngle().z * acceleration);
			}
			// y velocity is handled separately to stop the player from falling from the sky when they reach maximum
			// horizontal speed.
			if(Math.abs(caster.getDeltaMovement().y) < speed || caster.getDeltaMovement().y / caster.getLookAngle().y < 0){
				caster.push(0, caster.getLookAngle().y * acceleration + Y_NUDGE_ACCELERATION, 0);
			}

			if(!Wizardry.settings.replaceVanillaFallDamage) caster.fallDistance = 0.0f;
		}
		
		if(world.isClientSide){
			double x = caster.getX() - 1 + world.random.nextDouble() * 2;
			double y = caster.getY() + caster.getEyeHeight() - 0.5 + world.random.nextDouble();
			double z = caster.getZ() - 1 + world.random.nextDouble() * 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(0.8f, 1, 0.5f).spawn(world);
			x = caster.getX() - 1 + world.random.nextDouble() * 2;
			y = caster.getY() + caster.getEyeHeight() - 0.5 + world.random.nextDouble();
			z = caster.getZ() - 1 + world.random.nextDouble() * 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(1f, 1f, 1f).spawn(world);
		}
		
		if(ticksInUse % 24 == 0) playSound(world, caster, ticksInUse, -1, modifiers);
		
		return true;
	}

}
