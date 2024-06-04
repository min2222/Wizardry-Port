package electroblob.wizardry.data;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketGlyphData;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

/**
 * Class responsible for generating and storing the randomised spell names and descriptions for each world, which are
 * displayed as glyphs using the SGA font renderer.
 * 
 * @since Wizardry 1.1
 */
@Mod.EventBusSubscriber
public class SpellGlyphData extends WorldSavedData {

	public static final String NAME = Wizardry.MODID + "_glyphData";

	public Map<Spell, String> randomNames = new HashMap<>(Spell.getTotalSpellCount());
	public Map<Spell, String> randomDescriptions = new HashMap<>(Spell.getTotalSpellCount());

	// Required constructors
	public SpellGlyphData(){
		this(NAME);
	}

	public SpellGlyphData(String name){
		super(name);
	}

	/** Generates random names and descriptions for any spells which don't already have them. */
	public void generateGlyphNames(Level world){

		for(Spell spell : Spell.getAllSpells()){
			if(!randomNames.containsKey(spell)) randomNames.put(spell, generateRandomName(world.rand));
		}

		for(Spell spell : Spell.getAllSpells()){
			if(!randomDescriptions.containsKey(spell))
				randomDescriptions.put(spell, generateRandomDescription(world.rand));
		}

		this.markDirty();
	}

	private String generateRandomName(Random random){

		String name = "";

		for(int i = 0; i < random.nextInt(2) + 2; i++){
			name = name + RandomStringUtils.random(3 + random.nextInt(5), "abcdefghijklmnopqrstuvwxyz") + " ";
		}

		return name.trim();
	}

	private String generateRandomDescription(Random random){

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
	public static SpellGlyphData get(Level world){

		SpellGlyphData instance = (SpellGlyphData)world.loadData(SpellGlyphData.class, NAME);

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
			world.setData(NAME, instance);
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

		WizardryPacketHandler.net.sendTo(msg, player);

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

	@Override
	public void readFromNBT(CompoundTag nbt){

		this.randomNames = new HashMap<>();
		this.randomDescriptions = new HashMap<>();

		ListTag tagList = nbt.getTagList("spellGlyphData", NBT.TAG_COMPOUND);

		for(int i = 0; i < tagList.tagCount(); i++){
			CompoundTag tag = tagList.getCompoundTagAt(i);
			randomNames.put(Spell.byMetadata(tag.getInt("spell")), tag.getString("name"));
			randomDescriptions.put(Spell.byMetadata(tag.getInt("spell")), tag.getString("description"));
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag nbt){

		ListTag tagList = new ListTag();

		for(Spell spell : Spell.getAllSpells()){
			// Much like the enchantments tag for items, this stores a list of spell-id-to-name tag pairs
			// The description is now also included; there's no point in making a second compound tag!
			CompoundTag tag = new CompoundTag();
			tag.putInt("spell", spell.metadata());
			tag.setString("name", this.randomNames.get(spell));
			tag.setString("description", this.randomDescriptions.get(spell));
			tagList.appendTag(tag);
		}

		NBTExtras.storeTagSafely(nbt, "spellGlyphData", tagList);

		return nbt;
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(WorldEvent.Load event){
		if(!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0){
			// Called to initialise the spell glyph data when a world loads, if it isn't already.
			SpellGlyphData.get(event.getWorld());
		}
	}
}