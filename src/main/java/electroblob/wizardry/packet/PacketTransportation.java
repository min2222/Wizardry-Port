package electroblob.wizardry.packet;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a player is teleported due to the transportation spell to spawn
 * the particles.
 */
public class PacketTransportation {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){
		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleTransportationPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		/** The destination that was teleported to */
		public BlockPos destination;
		public int dismountEntityID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(BlockPos destination, @Nullable Entity toDismount){
			this.destination = destination;
			this.dismountEntityID = toDismount == null ? -1 : toDismount.getId();
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.destination = BlockPos.of(buf.readLong());
			this.dismountEntityID = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeLong(destination.asLong());
			buf.writeInt(dismountEntityID);
		}
	}
}
