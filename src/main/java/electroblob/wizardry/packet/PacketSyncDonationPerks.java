package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.misc.DonationPerksHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent to update the donation perks map for each player that logs in, and for
 * all players whenever the map changes.
 */
public class PacketSyncDonationPerks {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> DonationPerksHandler.setElements(message.elements));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public List<Element> elements;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(List<Element> elements){
			this.elements = elements;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.elements = new ArrayList<>();
			while(buf.isReadable()){
				int i = buf.readShort();
				elements.add(i == -1 ? null : Element.values()[i]);
			}
		}

		public void toBytes(FriendlyByteBuf buf){
			for(Element element : elements) buf.writeShort(element == null ? -1 : element.ordinal());
		}
	}
}
