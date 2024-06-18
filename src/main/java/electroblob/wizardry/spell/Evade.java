package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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

		if(!caster.isOnGround()) return false; // Prevents cheesing with cooldown upgrades to effectively fly super-fast

		Vec3 look = caster.getLookAngle();
		// We want a horizontal only vector
		look = look.subtract(0, look.y, 0).normalize();

		Vec3 evadeDirection;
		if(caster.xxa  == 0){
			// If the caster isn't strafing, pick a random direction
			evadeDirection = look.yRot(world.random.nextBoolean() ? (float)Math.PI/2f : (float)-Math.PI/2f);
		}else{
			// Otherwise, evade always moves whichever direction the caster was already strafing
			evadeDirection = look.yRot(Math.signum(caster.xxa ) * (float)Math.PI/2f);
		}

		evadeDirection = evadeDirection.scale(getProperty(EVADE_VELOCITY).floatValue() * modifiers.get(SpellModifiers.POTENCY));
		caster.push(evadeDirection.x, UPWARD_VELOCITY, evadeDirection.z);

		return true;
	}

}
