package electroblob.wizardry.constants;

import java.util.Random;
import java.util.function.Supplier;

import electroblob.wizardry.Wizardry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber
public class Tier {

	public static final Tier NOVICE = new Tier(700, 3, 12, Style.EMPTY.withColor(ChatFormatting.WHITE), "novice");
	public static final Tier APPRENTICE = new Tier(1000, 5, 5, Style.EMPTY.withColor(ChatFormatting.AQUA), "apprentice");
	public static final Tier ADVANCED = new Tier(1500, 7, 2, Style.EMPTY.withColor(ChatFormatting.DARK_BLUE), "advanced");
	public static final Tier MASTER = new Tier(2500, 9, 1, Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE), "master");
	
    public static Supplier<IForgeRegistry<Tier>> registry = null;
    
    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        RegistryBuilder<Tier> builder = new RegistryBuilder<>();
        builder.setName(new ResourceLocation(Wizardry.MODID, "tier"));
        builder.setIDRange(0, 5000);
        registry = event.create(builder);
    }
    
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(registry.get().getRegistryKey(), helper -> {
            helper.register("novice", NOVICE);
            helper.register("apprentice", APPRENTICE);
            helper.register("advanced", ADVANCED);
            helper.register("master", MASTER);
        });
    }

	/** Maximum mana a wand of this tier can store. */
	public final int maxCharge;
	/** Just an ordinal. Shouldn't really be needed but no point changing it now. */
	public final int level;
	/** The maximum number of upgrades that can be applied to a wand of this tier. */
	public final int upgradeLimit;
	/** The weight given to this tier in the standard weighting. */
	public final int weight;
	/** The colour of text associated with this tier. */
	// Changed to a Style object for consistency.
	private final Style colour;

	private final String unlocalisedName;

	public Tier(int maxCharge, int upgradeLimit, int weight, Style colour, String name){
		this.maxCharge = maxCharge;
		this.level = ordinal();
		this.upgradeLimit = upgradeLimit;
		this.weight = weight;
		this.colour = colour;
		this.unlocalisedName = name;
	}

	/** Returns the tier with the given name, or throws an {@link java.lang.IllegalArgumentException} if no such
	 * tier exists. */
	public static Tier fromName(String name){

		for(Tier tier : values()){
			if(tier.unlocalisedName.equals(name)) return tier;
		}

		throw new IllegalArgumentException("No such tier with unlocalised name: " + name);
	}

	/** Returns the tier above this one, or the same tier if this is the highest tier. */
	public Tier next(){
		return ordinal() + 1 < values().length ? values()[ordinal() + 1] : this;
	}

	/** Returns the tier below this one, or the same tier if this is the lowest tier. */
	public Tier previous(){
		return ordinal() > 0 ? values()[ordinal() - 1] : this;
	}

	/** Returns the translated display name of this tier, without formatting. */
	public String getDisplayName(){
		return Wizardry.proxy.translate("tier." + unlocalisedName);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the tier, without
	 * formatting (i.e. not coloured).
	 */
	public MutableComponent getNameForTranslation(){
		return Component.translatable("tier." + unlocalisedName);
	}

	/** Returns the translated display name of this tier, with formatting. */
	public String getDisplayNameWithFormatting(){
		return Wizardry.proxy.translate("tier." + unlocalisedName, this.colour);
	}

	/**
	 * Returns a {@code TextComponentTranslation} which will be translated to the display name of the tier, with
	 * formatting (i.e. coloured).
	 */
	public Component getNameForTranslationFormatted(){
		return Component.translatable("tier." + unlocalisedName).setStyle(this.colour);
	}

	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	public String getFormattingCode(){
		return colour.getFormattingCode();
	}

	/** The progression required for a wand to be upgraded to this tier. */
	public int getProgression(){
		return Wizardry.settings.progressionRequirements[this.ordinal() - 1];
	}

	/**
	 * Returns a random tier based on the standard weighting. Currently, the standard weighting is: Basic (Novice) 60%,
	 * Apprentice 25%, Advanced 10%, Master 5%. If an array of tiers is given, it picks a tier from the array, with the
	 * same relative weights for each. For example, if the array contains APPRENTICE and MASTER, then the weighting will
	 * become: Apprentice 83.3%, Master 16.7%.
	 */
	public static Tier getWeightedRandomTier(Random random, Tier... tiers){

		if(tiers.length == 0) tiers = values();

		int totalWeight = 0;

		for(Tier tier : tiers) totalWeight += tier.weight;

		int randomiser = random.nextInt(totalWeight);
		int cumulativeWeight = 0;

		for(Tier tier : tiers){
			cumulativeWeight += tier.weight;
			if(randomiser < cumulativeWeight) return tier;
		}

		// This will never happen, but it might as well be a sensible result.
		return tiers[tiers.length - 1];
	}

	public static Tier[] values() {
		return registry.get().getValues().toArray(new Tier[registry.get().getValues().size()]);
	}
	
	public int ordinal() {
		return ((ForgeRegistry<Tier>) registry.get()).getID(this);
	}
}
