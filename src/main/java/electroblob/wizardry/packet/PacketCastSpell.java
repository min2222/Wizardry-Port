package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastSpell.Message;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast by a player and returns true, and is sent to other
 * clients so they can spawn the particles themselves. What sending this packet effectively does is make the
 * {@link Item#use} method client-consistent just for the item that sends it. Interestingly,
 * {@link Item#onUsingTick} is client-consistent already, so continuous spells don't need to send packets from there
 * (this is probably something to do with eating particles or usage actions).
 */
public class PacketCastSpell implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().addScheduledTask(() -> Wizardry.proxy.handleCastSpellPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

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

		@Override
		public void fromBytes(ByteBuf buf){

			// The order is important
			this.casterID = buf.readInt();
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
			this.hand = buf.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		}

		@Override
		public void toBytes(ByteBuf buf){

			buf.writeInt(casterID);
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeBoolean(this.hand == InteractionHand.OFF_HAND);
		}
	}
}
