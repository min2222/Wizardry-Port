package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a shrine is conquered to update nearby clients and spawn particles.
 */
public class PacketConquerShrine {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleConquerShrinePacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public int x;
		public int y;
		public int z;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(BlockPos pos){
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.x = buf.readInt();
			this.y = buf.readInt();
			this.z = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
		}
	}
}
