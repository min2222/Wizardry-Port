package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityLectern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Client -> Server]</b> This packet is sent when a player closes the lectern GUI to send the last-viewed spell to
 * the server. */
public class PacketLectern {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isServer()){

			ctx.get().enqueueWork(() -> {
				final ServerPlayer player = ctx.get().getSender();
				
				BlockEntity tileentity = player.level.getBlockEntity(message.pos);

				if(tileentity instanceof TileEntityLectern){

					((TileEntityLectern)tileentity).currentSpell = message.spell;
					((TileEntityLectern)tileentity).sync(); // Update other clients with the new state

				}else{
					Wizardry.logger.warn("Received a PacketLectern, but no lectern existed at the position specified!");
				}
			});
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		private BlockPos pos;
		private Spell spell;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(BlockPos pos, Spell spell){
			this.pos = pos;
			this.spell = spell;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			spell = Spell.byNetworkID(buf.readInt());
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
			buf.writeInt(spell.networkID());
		}
	}
}
