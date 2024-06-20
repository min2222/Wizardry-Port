package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

/**
 * <b>[Server -> Client]</b> This packet is sent to synchronise the config settings with clients on player login.
 * 
 * @see Settings
 */
public class PacketSyncSettings {

	public static boolean onMessage(Message message, Supplier<NetworkEvent.Context> ctx){

		if(ctx.get().getDirection().getReceptionSide().isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// any more than necessary.
			net.minecraft.client.Minecraft.getInstance().doRunTask(() -> copySettings(message));
		}
		
		ctx.get().setPacketHandled(true);

		return true;
	}

	private static void copySettings(Message message){
		Wizardry.settings.discoveryMode = message.settings.discoveryMode;
		Wizardry.settings.creativeBypassesArcaneLock = message.settings.creativeBypassesArcaneLock;
		Wizardry.settings.slowTimeAffectsPlayers = message.settings.slowTimeAffectsPlayers;
		Wizardry.settings.replaceVanillaFireballs = message.settings.replaceVanillaFireballs;
		Wizardry.settings.replaceVanillaFallDamage = message.settings.replaceVanillaFallDamage;
		Wizardry.settings.forfeitChance = message.settings.forfeitChance;
		Wizardry.settings.progressionRequirements = message.settings.progressionRequirements;
		Wizardry.settings.bookshelfSearchRadius = message.settings.bookshelfSearchRadius;
		Wizardry.settings.bookshelfBlocks = message.settings.bookshelfBlocks;
		Wizardry.settings.bookItems = message.settings.bookItems;
		Wizardry.settings.passiveMobsAreAllies = message.settings.passiveMobsAreAllies;
	}

	public static class Message {

		/** Instance of wizardry's settings object */
		public Settings settings;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Settings settings){
			this.settings = settings;
		}

		public Message(FriendlyByteBuf buf){
			// I'm guessing the settings field will be null here, so it needs initialising.
			// This is also a great reason to have the settings as an actual object.
			settings = new Settings();
			// The order is important
			settings.discoveryMode = buf.readBoolean();
			settings.creativeBypassesArcaneLock = buf.readBoolean();
			settings.slowTimeAffectsPlayers = buf.readBoolean();
			settings.replaceVanillaFireballs = buf.readBoolean();
			settings.replaceVanillaFallDamage = buf.readBoolean();
			settings.forfeitChance = buf.readFloat();
			settings.progressionRequirements = new int[Tier.values().length - 1];
			for(int i = 0; i < settings.progressionRequirements.length; i++) settings.progressionRequirements[i] = buf.readInt();
			settings.bookshelfSearchRadius = buf.readInt();
			settings.bookshelfBlocks = readMetaItems(buf);
			settings.bookItems = readMetaItems(buf);
			settings.passiveMobsAreAllies = buf.readBoolean();
		}

		public void toBytes(FriendlyByteBuf buf){
			buf.writeBoolean(settings.discoveryMode);
			buf.writeBoolean(settings.creativeBypassesArcaneLock);
			buf.writeBoolean(settings.slowTimeAffectsPlayers);
			buf.writeBoolean(settings.replaceVanillaFireballs);
			buf.writeBoolean(settings.replaceVanillaFallDamage);
			buf.writeFloat((float)settings.forfeitChance); // Configs don't have floats but this can only be 0-1 anyway
			for(int i = 0; i < settings.progressionRequirements.length; i++) buf.writeInt(settings.progressionRequirements[i]);
			buf.writeInt(settings.bookshelfSearchRadius);
			writeMetaItems(buf, settings.bookshelfBlocks);
			writeMetaItems(buf, settings.bookItems);
			buf.writeBoolean(settings.passiveMobsAreAllies);
		}

		@SuppressWarnings("unchecked")
		private static Pair<ResourceLocation, Short>[] readMetaItems(FriendlyByteBuf buf){
			int length = buf.readInt();
			List<Pair<ResourceLocation, Short>> entries = new ArrayList<>();
			for(int i=0; i<length; i++){
				entries.add(Pair.of(new ResourceLocation(buf.readUtf()), buf.readShort()));
			}
			return entries.toArray(new Pair[0]);
		}

		private static void writeMetaItems(FriendlyByteBuf buf, Pair<ResourceLocation, Short>[] items){
			buf.writeInt(items.length);
			for(Pair<ResourceLocation, Short> entry : items){
				buf.writeUtf(entry.getLeft().toString());
				buf.writeShort(entry.getRight());
			}
		}

	}
}
