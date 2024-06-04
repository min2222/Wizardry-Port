package electroblob.wizardry.event;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * ImbuementActivateEvent is fired when {@link TileEntityImbuementAltar#getImbuementResult(ItemStack, electroblob.wizardry.constants.Element[],
 * boolean, Level, Player)} is called to allow adding in imbuement "recipes" dynamically, based on the
 * contents of the Imbuement altar's receptacles and the item placed on the altar.
 *
 * <i>Note that this event is only fired on the server side.</i><br>
 * <br>
 * To alter the result of the imbuement process, change the {@link electroblob.wizardry.event.ImbuementActivateEvent#result} result of the process.<br>
 * <br>
 * This event is {@link Cancelable}. If this event is canceled, no further processing takes place.<br>
 * <br>
 * This event does not have a result. {@link Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author WinDanesz
 * @since Wizardry 4.3.5
 */
@Cancelable
public class ImbuementActivateEvent extends Event {

	/** The item stack being imbued */
	public ItemStack input;

	/** The elements of the four receptacles */
	public Element[] receptacleElements;

	/** A reference to the current world object (may be null if {@code fullLootGen} is false) */
	public Level world;

	/** The player that last interacted with the imbuement altar, or null if there isn't one (or if this is being queried for other reasons, e.g. JEI) */
	public Player lastUser;

	/** The resulting item stack of the imbuement process */
	public ItemStack result;

	public ImbuementActivateEvent(ItemStack input, Element[] receptacleElements, Level world, Player lastUser, ItemStack result) {
		super();
		this.input = input;
		this.receptacleElements = receptacleElements;
		this.world = world;
		this.lastUser = lastUser;
		this.result = result;
	}
}