package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast at a position by commands and returns true, and is
 * sent to clients so they can spawn the particles themselves.
 */
// Soooo many spell casting packets...
public class PacketCastSpellAtPos {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleCastSpellAtPosPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		/** Position for the spell */
		public Vec3 position;
		/** Direction for the spell */
		public Direction direction;
		/** ID of the spell being cast */
		public int spellID;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;
		/** Number of ticks to cast the spell for, or -1 for non-continuous spells */
		public int duration;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Vec3 position, Direction direction, Spell spell, SpellModifiers modifiers){
			this(position, direction, spell, modifiers, -1);
		}

		public Message(Vec3 position, Direction direction, Spell spell, SpellModifiers modifiers, int duration){
			this.spellID = spell.networkID();
			this.modifiers = modifiers;
			this.position = position;
			this.direction = direction;
			this.duration = duration;
		}

		public Message(FriendlyByteBuf buf){

			// The order is important
			position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			direction = Direction.from3DDataValue(buf.readInt());
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
			this.duration = buf.readInt();
		}

		public void toBytes(FriendlyByteBuf buf){

			buf.writeDouble(position.x);
			buf.writeDouble(position.y);
			buf.writeDouble(position.z);
			buf.writeInt(direction.get3DDataValue());
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeInt(duration);
		}
	}
}
