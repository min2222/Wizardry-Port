package electroblob.wizardry.packet;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Server -> Client]</b> This packet is sent when a player possesses an entity or when a player stops possessing
 * to update all clients. */
public class PacketPossession {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handlePossessionPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public int playerID;
		public int targetID;
		public int duration;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Player host, @Nullable Mob target, int duration){
			this.playerID = host.getId();
			this.targetID = target == null ? -1 : target.getId();
			this.duration = duration;
		}

		public Message(FriendlyByteBuf buf){
			this.playerID = buf.readInt();
			this.targetID = buf.readInt();
			this.duration = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(playerID);
			buf.writeInt(targetID);
			buf.writeInt(duration);
		}
	}
}
