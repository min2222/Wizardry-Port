package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.misc.DonationPerksHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Client -> Server]</b> This packet is sent when a player logs in and whenever the setting changes to set their
 * donation perk element. */
public class PacketRequestDonationPerks {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isServer()){

			ctx.get().enqueueWork(() -> {
				final ServerPlayer player = ctx.get().getSender();

				// The UUID key set itself is immutable so we can safely access it from the networking thread
				if(DonationPerksHandler.isDonor(player)){
					DonationPerksHandler.setElement(player, message.element);
				}else{
					Wizardry.logger.warn("Received a donation perk packet from a player that isn't a donor!");
				}
			});

		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		private Element element;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Element element){
			this.element = element;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			int i = buf.readShort();
			element = i == -1 ? null : Element.values()[i];
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeShort(element == null ? -1 : element.ordinal());
		}
	}
}
