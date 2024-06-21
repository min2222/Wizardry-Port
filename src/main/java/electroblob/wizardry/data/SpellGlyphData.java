package electroblob.wizardry.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketGlyphData;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Class responsible for generating and storing the randomised spell names and descriptions for each world, which are
 * displayed as glyphs using the SGA font renderer.
 * 
 * @since Wizardry 1.1
 */
@Mod.EventBusSubscriber
public class SpellGlyphData extends SavedData {

	public static final String NAME = Wizardry.MODID + "_glyphData";

	public Map<Spell, String> randomNames = new HashMap<>(Spell.getTotalSpellCount());
	public Map<Spell, String> randomDescriptions = new HashMap<>(Spell.getTotalSpellCount());

	// Required constructors
	public SpellGlyphData(){
		this(NAME);
	}

	public SpellGlyphData(String name){
		
	}

	/** Generates random names and descriptions for any spells which don't already have them. */
	public void generateGlyphNames(Level world){

		for(Spell spell : Spell.getAllSpells()){
			if(!randomNames.containsKey(spell)) randomNames.put(spell, generateRandomName(world.random));
		}

		for(Spell spell : Spell.getAllSpells()){
			if(!randomDescriptions.containsKey(spell))
				randomDescriptions.put(spell, generateRandomDescription(world.random));
		}

		this.setDirty();
	}

	private String generateRandomName(RandomSource random){

		String name = "";

		for(int i = 0; i < random.nextInt(2) + 2; i++){
			name = name + RandomStringUtils.random(3 + random.nextInt(5), "abcdefghijklmnopqrstuvwxyz") + " ";
		}

		return name.trim();
	}

	private String generateRandomDescription(RandomSource random){

		String name = "";

		for(int i = 0; i < random.nextInt(16) + 8; i++){
			name = name + RandomStringUtils.random(2 + random.nextInt(7), "abcdefghijklmnopqrstuvwxyz") + " ";
		}

		return name.trim();
	}

	/**
	 * Returns the spell glyph data for this world, or creates a new instance if it doesn't exist yet. Also checks for
	 * any spells that are missing glyph data and adds it accordingly.
	 */
	public static SpellGlyphData get(ServerLevel world){

		SpellGlyphData instance = (SpellGlyphData)world.getDataStorage().get(SpellGlyphData::load, NAME);

		if(instance == null){
			instance = new SpellGlyphData();
		}

		// These two conditions are a bit of backwards compatibility from when I added the descriptions to the
		// glyph data. Shouldn't be needed in normal operation, but I might as well leave it here.
		// Edit: More backwards compatibility, this time for the future - should any new spells be added, this now
		// ensures
		// existing worlds will generate random names and descriptions for any new spells whilst keeping the old ones.
		if(instance.randomNames.size() < Spell.getTotalSpellCount()
				|| instance.randomDescriptions.size() < Spell.getTotalSpellCount()){
			instance.generateGlyphNames(world);
			world.getDataStorage().set(NAME, instance);
		}

		return instance;
	}

	/** Sends the random spell names for this world to the specified player's client. */
	public void sync(ServerPlayer player){

		List<String> names = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();

		int id = 0;

		while(id < Spell.getTotalSpellCount()){
			Spell spell = Spell.byNetworkID(id + 1); // +1 because the None spell is not included
			names.add(this.randomNames.get(spell));
			descriptions.add(this.randomDescriptions.get(spell));
			id++;
		}

		PacketGlyphData.Message msg = new PacketGlyphData.Message(names, descriptions);

		WizardryPacketHandler.net.send(PacketDistributor.PLAYER.with(() -> player), msg);

		Wizardry.logger.info("Synchronising spell glyph data for " + player.getName());

	}

	/** Helper method to retrieve the random glyph name for the given spell from the map stored in the given world. */
	public static String getGlyphName(Spell spell, Level world){
		Map<Spell, String> names = SpellGlyphData.get(world).randomNames;
		return names == null ? "" : names.get(spell);
	}

	/**
	 * Helper method to retrieve the random glyph description for the given spell from the map stored in the given
	 * world.
	 */
	public static String getGlyphDescription(Spell spell, Level world){
		Map<Spell, String> descriptions = SpellGlyphData.get(world).randomDescriptions;
		return descriptions == null ? "" : descriptions.get(spell);
	}

	public static SpellGlyphData load(CompoundTag nbt){
        SpellGlyphData data = new SpellGlyphData();
        data.randomNames = new HashMap<>();
        data.randomDescriptions = new HashMap<>();

        ListTag tagList = nbt.getList("spellGlyphData", Tag.TAG_COMPOUND);

        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag tag = tagList.getCompound(i);
            data.randomNames.put(Spell.byMetadata(tag.getInt("spell")), tag.getString("name"));
            data.randomDescriptions.put(Spell.byMetadata(tag.getInt("spell")), tag.getString("description"));
        }
        return data;
	}

	@Override
	public CompoundTag save(CompoundTag nbt){

		ListTag tagList = new ListTag();

		for(Spell spell : Spell.getAllSpells()){
			// Much like the enchantments tag for items, this stores a list of spell-id-to-name tag pairs
			// The description is now also included; there's no point in making a second compound tag!
			CompoundTag tag = new CompoundTag();
			tag.putInt("spell", spell.metadata());
			tag.putString("name", this.randomNames.get(spell));
			tag.putString("description", this.randomDescriptions.get(spell));
			tagList.add(tag);
		}

		NBTExtras.storeTagSafely(nbt, "spellGlyphData", tagList);

		return nbt;
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(LevelEvent.Load event){
		if(!event.getLevel().isClientSide() && ((Level) event.getLevel()).dimension() == Level.OVERWORLD){
			// Called to initialise the spell glyph data when a world loads, if it isn't already.
			SpellGlyphData.get((Level) event.getLevel());
		}
	}
}