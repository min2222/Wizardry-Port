package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent each time a player joins a world to synchronise the client-side spell
 * glyph names with the server-side ones.
 */
public class PacketGlyphData {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){
		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleGlyphDataPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		/** The list of randomised spell names. */
		public List<String> names;
		/** The list of randomised spell descriptions. */
		public List<String> descriptions;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(List<String> names, List<String> descriptions){
			this.names = names;
			this.descriptions = descriptions;
		}

		public Message(FriendlyByteBuf buf){
			names = new ArrayList<>();
			descriptions = new ArrayList<>();
			while(buf.isReadable()){
				names.add(buf.readUtf());
				descriptions.add(buf.readUtf());
			}
		}

		public void toBytes(FriendlyByteBuf buf){
			for(int i = 0; i < names.size(); i++){
				buf.writeUtf(names.get(i) == null ? "error" : names.get(i));
				buf.writeUtf(descriptions.get(i) == null ? "error" : descriptions.get(i));
			}
		}
	}
}
