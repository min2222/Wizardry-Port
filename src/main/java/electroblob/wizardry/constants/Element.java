package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public enum Element implements StringRepresentable {

	/** The 'default' element, with {@link electroblob.wizardry.registry.Spells#magic_missile magic missile} being its
	 * only spell. */
	MAGIC(new Style().setColor(ChatFormatting.GRAY), "magic"),
	FIRE(new Style().setColor(ChatFormatting.DARK_RED), "fire"),
	ICE(new Style().setColor(ChatFormatting.AQUA), "ice"),
	LIGHTNING(new Style().setColor(ChatFormatting.DARK_AQUA), "lightning"),
	NECROMANCY(new Style().setColor(ChatFormatting.DARK_PURPLE), "necromancy"),
	EARTH(new Style().setColor(ChatFormatting.DARK_GREEN), "earth"),
	SORCERY(new Style().setColor(ChatFormatting.GREEN), "sorcery"),
	HEALING(new Style().setColor(ChatFormatting.YELLOW), "healing");

	/** Display colour for this element */
	private final Style colour;
	/** Unlocalised name for this element */
	private final String unlocalisedName;
	/** The {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI) */
	private final ResourceLocation icon;

	Element(Style colour, String name){
		this(colour, name, Wizardry.MODID);
	}

	Element(Style colour, String name, String modid){
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
	public String getDisplayName(){
		return Wizardry.proxy.translate("element." + getName());
	}

	/** Returns the {@link Style} object representing the colour of this element. */
	public Style getColour(){
		return colour;
	}

	/** Returns the string formatting code which corresponds to the colour of this element. */
	public String getFormattingCode(){
		return colour.getFormattingCode();
	}

	/** Returns the translated display name for wizards of this element, shown in the trading GUI. */
	public Component getWizardName(){
		return Component.translatable("element." + getName() + ".wizard");
	}

	/** Returns this element's unlocalised name. Also used as the serialised string in block properties. */
	@Override
	public String getName(){
		return unlocalisedName;
	}

	/** Returns the {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI). */
	public ResourceLocation getIcon(){
		return icon;
	}
}
