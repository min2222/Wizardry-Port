package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketGlyphData.Message;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>[Server -> Client]</b> This packet is sent each time a player joins a world to synchronise the client-side spell
 * glyph names with the server-side ones.
 */
public class PacketGlyphData implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().addScheduledTask(() -> Wizardry.proxy.handleGlyphDataPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

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

		@Override
		public void fromBytes(ByteBuf buf){
			names = new ArrayList<>();
			descriptions = new ArrayList<>();
			while(buf.isReadable()){
				names.add(ByteBufUtils.readUTF8String(buf));
				descriptions.add(ByteBufUtils.readUTF8String(buf));
			}
		}

		@Override
		public void toBytes(ByteBuf buf){
			for(int i = 0; i < names.size(); i++){
				ByteBufUtils.writeUTF8String(buf, names.get(i) == null ? "error" : names.get(i));
				ByteBufUtils.writeUTF8String(buf, descriptions.get(i) == null ? "error" : descriptions.get(i));
			}
		}
	}
}
