package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class Evade extends Spell {

	private static final String EVADE_VELOCITY = "evade_velocity";

	private static final float UPWARD_VELOCITY = 0.25f;

	public Evade(){
		super("evade", UseAnim.NONE, false);
		addProperties(EVADE_VELOCITY);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!caster.onGround) return false; // Prevents cheesing with cooldown upgrades to effectively fly super-fast

		Vec3 look = caster.getLookVec();
		// We want a horizontal only vector
		look = look.subtract(0, look.y, 0).normalize();

		Vec3 evadeDirection;
		if(caster.moveStrafing == 0){
			// If the caster isn't strafing, pick a random direction
			evadeDirection = look.rotateYaw(world.rand.nextBoolean() ? (float)Math.PI/2f : (float)-Math.PI/2f);
		}else{
			// Otherwise, evade always moves whichever direction the caster was already strafing
			evadeDirection = look.rotateYaw(Math.signum(caster.moveStrafing) * (float)Math.PI/2f);
		}

		evadeDirection = evadeDirection.scale(getProperty(EVADE_VELOCITY).floatValue() * modifiers.get(SpellModifiers.POTENCY));
		caster.addVelocity(evadeDirection.x, UPWARD_VELOCITY, evadeDirection.z);

		return true;
	}

}
