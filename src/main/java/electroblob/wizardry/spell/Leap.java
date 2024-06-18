package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class Leap extends Spell {

	public static final String HORIZONTAL_SPEED = "horizontal_speed";
	public static final String VERTICAL_SPEED = "vertical_speed";

	public Leap(){
		super("leap", UseAnim.NONE, false);
		addProperties(HORIZONTAL_SPEED, VERTICAL_SPEED);
		soundValues(0.5f, 1, 0);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.isOnGround()){

			caster.setDeltaMovement(caster.getDeltaMovement().x, getProperty(VERTICAL_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY), caster.getDeltaMovement().z);
			double horizontalSpeed = getProperty(HORIZONTAL_SPEED).floatValue();
			caster.push(caster.getLookAngle().x * horizontalSpeed, 0, caster.getLookAngle().z * horizontalSpeed);

			if(world.isClientSide){
				for(int i = 0; i < 10; i++){
					double x = caster.getX() + world.random.nextFloat() - 0.5F;
					double y = caster.getY();
					double z = caster.getZ() + world.random.nextFloat() - 0.5F;
					world.addParticle(ParticleTypes.CLOUD, x, y, z, 0, 0, 0);
				}
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swing(hand);
			return true;
		}

		return false;
	}

}
