package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent to clients in the same dimension when a player is resurrected.
 */
public class PacketResurrection {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){
		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleResurrectionPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public int playerID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(int playerID){
			this.playerID = playerID;
		}

		public Message(FriendlyByteBuf buf){
			this.playerID = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(playerID);
		}
	}
}
