package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Server -> Client]</b> This packet is fired on login and on advancement gain to update the handbook progress. */
public class PacketSyncAdvancements {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleAdvancementSyncPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		public boolean showToasts;
		public ResourceLocation[] completedAdvancements;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(boolean showToasts, ResourceLocation... completed){
			this.showToasts = showToasts;
			this.completedAdvancements = completed;
		}

		public Message(FriendlyByteBuf buf){
			showToasts = buf.readBoolean();
			ArrayList<ResourceLocation> advancements = new ArrayList<>();
			while(buf.isReadable()){
				advancements.add(new ResourceLocation(buf.readUtf()));
			}
			this.completedAdvancements = advancements.toArray(new ResourceLocation[0]);
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeBoolean(showToasts);
			for(ResourceLocation advancement : completedAdvancements){
				buf.writeUtf(advancement.toString());
			}
		}
	}
}
