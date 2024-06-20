package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.item.ISpellCastingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Client -> Server]</b> This packet is for the spell quick access keys. */
public class PacketSpellQuickAccess {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isServer()){

			final ServerPlayer player = ctx.get().getSender();

			ctx.get().enqueueWork(() -> {
				ItemStack wand = player.getMainHandItem();

				if(!(wand.getItem() instanceof ISpellCastingItem)){
					wand = player.getOffhandItem();
				}

				if(wand.getItem() instanceof ISpellCastingItem){

					((ISpellCastingItem)wand.getItem()).selectSpell(wand, message.index);
					// This line fixes the bug with continuous spells casting when they shouldn't be
					player.stopUsingItem();
				}
			});
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		private int index;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int index){
			this.index = index;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.index = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(index);
		}
	}
}
