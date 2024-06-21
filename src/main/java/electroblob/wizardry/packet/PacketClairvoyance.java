package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a player casts the clairvoyance spell to allow pathing to chunks
 * outside the render distance.
 */
public class PacketClairvoyance {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){
		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(new Runnable(){
				@Override
				public void run(){
					Wizardry.proxy.handleClairvoyancePacket(message);
				}
			});
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public Path path;
		public float durationMultiplier;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Path path, float durationMultiplier){

			this.path = path;
			this.durationMultiplier = durationMultiplier;
		}

		public Message(FriendlyByteBuf buf){

			// The order is important
			this.durationMultiplier = buf.readFloat();

			List<Node> points = new ArrayList<Node>();

			while(buf.isReadable()){
				points.add(new Node(buf.readInt(), buf.readInt(), buf.readInt()));
			}

			this.path = new Path(points, new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()), false);
		}

		public void toBytes(FriendlyByteBuf buf){

			buf.writeFloat(durationMultiplier);

			for(int i = 0; i < path.getNodeCount(); i++){

				Node point = path.getNode(i);

				buf.writeInt(point.x);
				buf.writeInt(point.y);
				buf.writeInt(point.z);
			}
		}
	}
}
