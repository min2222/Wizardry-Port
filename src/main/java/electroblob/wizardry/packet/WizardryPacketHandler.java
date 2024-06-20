package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class WizardryPacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel net = 
    		NetworkRegistry.newSimpleChannel(new ResourceLocation(Wizardry.MODID, "ebwizardry"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	private static int nextPacketId = 0;
	
	public static void initPackets(){
		net.registerMessage(nextPacketId++, PacketControlInput.Message.class,           PacketControlInput.Message::toBytes, PacketControlInput.Message::new, PacketControlInput::onMessage);
		net.registerMessage(nextPacketId++, PacketCastSpell.Message.class,              PacketCastSpell.Message::toBytes, PacketCastSpell.Message::new, PacketCastSpell::onMessage);
		net.registerMessage(nextPacketId++, PacketTransportation.Message.class, 		PacketTransportation.Message::toBytes, PacketTransportation.Message::new, PacketTransportation::onMessage);
		net.registerMessage(nextPacketId++, PacketPlayerSync.Message.class, 			PacketPlayerSync.Message::toBytes, PacketPlayerSync.Message::new, PacketPlayerSync::onMessage);
		net.registerMessage(nextPacketId++, PacketGlyphData.Message.class, 				PacketGlyphData.Message::toBytes, PacketGlyphData.Message::new, PacketGlyphData::onMessage);
		net.registerMessage(nextPacketId++, PacketCastContinuousSpell.Message.class, 	PacketCastContinuousSpell.Message::toBytes, PacketCastContinuousSpell.Message::new, PacketCastContinuousSpell::onMessage);
		net.registerMessage(nextPacketId++, PacketClairvoyance.Message.class, 			PacketClairvoyance.Message::toBytes, PacketClairvoyance.Message::new, PacketClairvoyance::onMessage);
		net.registerMessage(nextPacketId++, PacketSyncSettings.Message.class, 			PacketSyncSettings.Message::toBytes, PacketSyncSettings.Message::new, PacketSyncSettings::onMessage);
		net.registerMessage(nextPacketId++, PacketNPCCastSpell.Message.class, 			PacketNPCCastSpell.Message::toBytes, PacketNPCCastSpell.Message::new, PacketNPCCastSpell::onMessage);
		net.registerMessage(nextPacketId++, PacketDispenserCastSpell.Message.class, 	PacketDispenserCastSpell.Message::toBytes, PacketDispenserCastSpell.Message::new, PacketDispenserCastSpell::onMessage);
		net.registerMessage(nextPacketId++, PacketSpellProperties.Message.class, 		PacketSpellProperties.Message::toBytes, PacketSpellProperties.Message::new, PacketSpellProperties::onMessage);
		net.registerMessage(nextPacketId++, PacketSyncAdvancements.Message.class, 		PacketSyncAdvancements.Message::toBytes, PacketSyncAdvancements.Message::new, PacketSyncAdvancements::onMessage);
		net.registerMessage(nextPacketId++, PacketRequestAdvancementSync.Message.class, PacketRequestAdvancementSync.Message::toBytes, PacketRequestAdvancementSync.Message::new, PacketRequestAdvancementSync::onMessage);
		net.registerMessage(nextPacketId++, PacketResurrection.Message.class, 			PacketResurrection.Message::toBytes, PacketResurrection.Message::new, PacketResurrection::onMessage);
		net.registerMessage(nextPacketId++, PacketCastSpellAtPos.Message.class, 		PacketCastSpellAtPos.Message::toBytes, PacketCastSpellAtPos.Message::new, PacketCastSpellAtPos::onMessage);
		net.registerMessage(nextPacketId++, PacketEmitterData.Message.class, 			PacketEmitterData.Message::toBytes, PacketEmitterData.Message::new, PacketEmitterData::onMessage);
		net.registerMessage(nextPacketId++, PacketPossession.Message.class, 			PacketPossession.Message::toBytes, PacketPossession.Message::new, PacketPossession::onMessage);
		net.registerMessage(nextPacketId++, PacketConquerShrine.Message.class, 			PacketConquerShrine.Message::toBytes, PacketConquerShrine.Message::new, PacketConquerShrine::onMessage);
		net.registerMessage(nextPacketId++, PacketLectern.Message.class, 				PacketLectern.Message::toBytes, PacketLectern.Message::new, PacketLectern::onMessage);
		net.registerMessage(nextPacketId++, PacketSpellQuickAccess.Message.class, 		PacketSpellQuickAccess.Message::toBytes, PacketSpellQuickAccess.Message::new, PacketSpellQuickAccess::onMessage);
		net.registerMessage(nextPacketId++, PacketRequestDonationPerks.Message.class, 	PacketRequestDonationPerks.Message::toBytes, PacketRequestDonationPerks.Message::new, PacketRequestDonationPerks::onMessage);
		net.registerMessage(nextPacketId++, PacketSyncDonationPerks.Message.class, 		PacketSyncDonationPerks.Message::toBytes, PacketSyncDonationPerks.Message::new, PacketSyncDonationPerks::onMessage);
	}
}