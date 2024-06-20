package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** <b>[Server -> Client]</b> This packet is sent to sync server-side spell properties with clients on login. */
public class PacketSpellProperties {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){

			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> {
				for(int i=0; i<message.propertiesArray.length; i++){
					Spell.byNetworkID(i).setPropertiesClient(message.propertiesArray[i]);
				}
			});
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		private SpellProperties[] propertiesArray;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(SpellProperties... properties){
			this.propertiesArray = properties;
		}

		public Message(FriendlyByteBuf buf){

			List<SpellProperties> propertiesList = new ArrayList<>();
			int i = 0;

			while(buf.isReadable()){
				propertiesList.add(new SpellProperties(Spell.byNetworkID(i++), buf));
			}

			propertiesArray = propertiesList.toArray(new SpellProperties[0]);
		}

		public void toBytes(FriendlyByteBuf buf){
			for(SpellProperties properties : propertiesArray) properties.write(buf);
		}
	}
}
