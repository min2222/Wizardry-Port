package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when the /cast command is used with a continuous spell, in order to
 * sync the relevant variables in {@link WizardData WizardData}.
 */
public class PacketCastContinuousSpell {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleCastContinuousSpellPacket(message));
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
		/** Number of ticks to cast the spell for, or -1 for non-continuous spells */
		public int duration;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Player caster, Spell spell, SpellModifiers modifiers, int duration){
			this.casterID = caster.getId();
			this.spellID = spell.networkID();
			this.modifiers = modifiers;
			this.duration = duration;
		}

		public Message(FriendlyByteBuf buf){
			// The order is important
			this.casterID = buf.readInt();
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			modifiers.read(buf);
			this.duration = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeInt(casterID);
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeInt(duration);
		}
	}
}
