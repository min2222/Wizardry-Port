package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.item.EnumAction;
import net.minecraft.world.level.Level;

/**
 * This class represents a blank spell used to fill empty slots on wands. It is unobtainable in-game, except via
 * commands, and does nothing when the player attempts to cast it. Its instance can be referenced directly using
 * {@link electroblob.wizardry.registry.Spells#none Spells.none}
 */
public class None extends Spell {

	public None(){
		super("none", EnumAction.NONE, false);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
