package electroblob.wizardry.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * ResurrectionEvent is fired when a player is about to be resurrected, after all other checks have been performed.
 * <i>Note that this event is only fired on the server side.</i><br>
 * <br>
 * This event is {@link Cancelable}. If this event is canceled, the player is not resurrected.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Cancelable
public class ResurrectionEvent extends PlayerEvent {

	private final Player caster;

	public ResurrectionEvent(Player player, Player caster){
		super(player);
		this.caster = caster;
	}

	/** Returns the player that cast the resurrection spell. If the player resurrected themselves, this will be the
	 * same as {@link ResurrectionEvent#getEntity()}. */
	public Player getCaster(){
		return caster;
	}

}
