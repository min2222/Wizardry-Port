package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast by a player and returns true, and is sent to other
 * clients so they can spawn the particles themselves. What sending this packet effectively does is make the
 * {@link Item#use} method client-consistent just for the item that sends it. Interestingly,
 * {@link Item#onUsingTick} is client-consistent already, so continuous spells don't need to send packets from there
 * (this is probably something to do with eating particles or usage actions).
 */
public class PacketCastSpell {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleCastSpellPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		/** EntityID of the caster */
		public int casterID;
		/** ID of the spell being cast */
		public int spellID;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;
		/** The hand that is holding the itemstack used to cast the spell. Defaults to MAIN_HAND. */
		public InteractionHand hand;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int casterID, InteractionHand hand, Spell spell, SpellModifiers modifiers){

			this.casterID = casterID;
			this.spellID = spell.networkID();
			this.modifiers = modifiers;
			this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
		}

		public Message(FriendlyByteBuf buf){

			// The order is important
			this.casterID = buf.readInt();
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
			this.hand = buf.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		}

		public void toBytes(FriendlyByteBuf buf){

			buf.writeInt(casterID);
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeBoolean(this.hand == InteractionHand.OFF_HAND);
		}
	}
}
