package electroblob.wizardry.packet;

import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast by a dispenser and returns true, and is sent to
 * clients so they can spawn the particles. Unlike the player packets, this is for both continuous <b>and</b>
 * non-continuous spells.
 */
public class PacketDispenserCastSpell {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		// Just to make sure that the side is correct
		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> Wizardry.proxy.handleDispenserCastSpellPacket(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	public static class Message {

		/** ID of the spell being cast */
		public int spellID;
		/** Coordinates of the spell origin */
		public double x, y, z;
		/** Spell casting direction */
		public Direction direction;
		/** BlockPos of the block that cast this spell. <i>Not necessarily the same as the (x, y, z) coordinates.</i> */
		public BlockPos pos;
		/** The number of ticks to cast the spell for, or -1 if the spell should be cast until stopped. */
		public int duration;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(double x, double y, double z, Direction direction, BlockPos pos, Spell spell, int duration, SpellModifiers modifiers){

			this.x = x;
			this.y = y;
			this.z = z;
			this.direction = direction;
			this.pos = pos;
			this.spellID = spell.networkID();
			this.duration = duration;
			this.modifiers = modifiers;
			
		}

		public Message(FriendlyByteBuf buf){

			// The order is important
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.direction = Direction.values()[buf.readInt()];
			this.pos = BlockPos.of(buf.readLong());
			this.spellID = buf.readInt();
			this.duration = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
		}

		public void toBytes(FriendlyByteBuf buf){

			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeInt(direction.ordinal());
			buf.writeLong(pos.asLong());
			buf.writeInt(spellID);
			buf.writeInt(duration);
			this.modifiers.write(buf);
		}
	}
}
