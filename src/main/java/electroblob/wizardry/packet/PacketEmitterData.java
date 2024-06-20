package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.command.SpellEmitter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent each time a player joins a world to synchronise the client-side spell
 * emitters with the server-side ones.
 */
public class PacketEmitterData {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){
		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleEmitterDataPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public List<SpellEmitter> emitters;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(List<SpellEmitter> emitters){
			this.emitters = emitters;
		}

		public Message(FriendlyByteBuf buf){
			emitters = new ArrayList<>();
			while(buf.isReadable()){
				emitters.add(SpellEmitter.read(buf));
			}
		}

		public void toBytes(FriendlyByteBuf buf){
			emitters.forEach(s -> s.write(buf));
		}
	}
}
