package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.function.Supplier;

import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Client -> Server]</b> Fired on resource reload to request that the server re-sync the player's advancements. */
public class PacketRequestAdvancementSync {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isServer()){

			final ServerPlayer player = ctx.get().getSender();

			ArrayList<ResourceLocation> advancements = new ArrayList<>();

			for(Advancement advancement : player.getServer().getAdvancements().getAllAdvancements()){
				if(player.getAdvancements().getOrStartProgress(advancement).isDone()) advancements.add(advancement.getId());
			}
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}


	public static class Message {

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		// Don't need to put anything in here!

		public Message(FriendlyByteBuf buf){}

		public void toBytes(FriendlyByteBuf buf){}
	}
}
