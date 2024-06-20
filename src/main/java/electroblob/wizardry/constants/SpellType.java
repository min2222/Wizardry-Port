package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum SpellType {

	ATTACK("attack"),
	DEFENCE("defence"),
	UTILITY("utility"),
	MINION("minion"),
	BUFF("buff"),
	CONSTRUCT("construct"),
	PROJECTILE("projectile"),
	ALTERATION("alteration");

	private final String unlocalisedName;

	SpellType(String name){
		this.unlocalisedName = name;
	}

	/** Returns the spell type with the given name, or throws an {@link java.lang.IllegalArgumentException} if no such
	 * spell type exists. */
	public static SpellType fromName(String name){

		for(SpellType type : values()){
			if(type.unlocalisedName.equals(name)) return type;
		}

		throw new IllegalArgumentException("No such spell type with unlocalised name: " + name);
	}

	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	@OnlyIn(Dist.CLIENT)
	public Component getDisplayName(){
		return Wizardry.proxy.translate("spelltype." + unlocalisedName);
	}
}