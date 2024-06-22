package electroblob.wizardry.constants;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
public class Element {

	/** The 'default' element, with {@link electroblob.wizardry.registry.Spells#magic_missile magic missile} being its
	 * only spell. */
	public static final Element MAGIC = new Element(Style.EMPTY.withColor(ChatFormatting.GRAY), "magic");
	public static final Element FIRE = new Element(Style.EMPTY.withColor(ChatFormatting.DARK_RED), "fire");
	public static final Element ICE = new Element(Style.EMPTY.withColor(ChatFormatting.AQUA), "ice");
	public static final Element LIGHTNING = new Element(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA), "lightning");
	public static final Element NECROMANCY = new Element(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE), "necromancy");
	public static final Element EARTH = new Element(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN), "earth");
	public static final Element SORCERY = new Element(Style.EMPTY.withColor(ChatFormatting.GREEN), "sorcery");
	public static final Element HEALING = new Element(Style.EMPTY.withColor(ChatFormatting.YELLOW), "healing");
	
    public static Supplier<IForgeRegistry<Element>> registry = null;
    
    @SubscribeEvent
    public static void createRegistry(NewRegistryEvent event) {
        RegistryBuilder<Element> builder = new RegistryBuilder<>();
        builder.setName(new ResourceLocation(Wizardry.MODID, "element"));
        builder.setIDRange(0, 5000);
        registry = event.create(builder);
    }
    
    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(registry.get().getRegistryKey(), helper -> {
            helper.register("magic", MAGIC);
            helper.register("fire", FIRE);
            helper.register("ice", ICE);
            helper.register("lightning", LIGHTNING);
            helper.register("necromancy", NECROMANCY);
            helper.register("earth", EARTH);
            helper.register("sorcery", SORCERY);
            helper.register("healing", HEALING);
        });
    }

	/** Display colour for this element */
	private final Style colour;
	/** Unlocalised name for this element */
	private final String unlocalisedName;
	/** The {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI) */
	private final ResourceLocation icon;

	public Element(Style colour, String name){
		this(colour, name, Wizardry.MODID);
	}

	public Element(Style colour, String name, String modid){
		this.colour = colour;
		this.unlocalisedName = name;
		this.icon = new ResourceLocation(modid, "textures/gui/container/element_icon_" + unlocalisedName + ".png");
	}

	/** Returns the element with the given name, or throws an {@link java.lang.IllegalArgumentException} if no such
	 * element exists. */
	public static Element fromName(String name){

		for(Element element : values()){
			if(element.unlocalisedName.equals(name)) return element;
		}

		throw new IllegalArgumentException("No such element with unlocalised name: " + name);
	}

	/** Same as {@link Element#fromName(String)}, but returns the given fallback instead of throwing an exception if no
	 * element matches the given string. */
	@Nullable
	public static Element fromName(String name, @Nullable Element fallback){

		for(Element element : values()){
			if(element.unlocalisedName.equals(name)) return element;
		}

		return fallback;
	}

	/** Returns the translated display name of this element, without formatting. */
	public Component getDisplayName(){
		return Wizardry.proxy.translate("element." + getName());
	}

	/** Returns the {@link Style} object representing the colour of this element. */
	public Style getColour(){
		return colour;
	}

	/** Returns the translated display name for wizards of this element, shown in the trading GUI. */
	public Component getWizardName(){
		return Component.translatable("element." + getName() + ".wizard");
	}

	/** Returns this element's unlocalised name. Also used as the serialised string in block properties. */
	public String getName(){
		return unlocalisedName;
	}

	/** Returns the {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI). */
	public ResourceLocation getIcon(){
		return icon;
	}
	
	public static Element[] values() {
		return registry.get().getValues().toArray(new Element[registry.get().getValues().size()]);
	}
	
	public int ordinal() {
		return ((ForgeRegistry<Element>) registry.get()).getID(this);
	}
}
