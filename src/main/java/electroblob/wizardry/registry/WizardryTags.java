package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class WizardryTags {
	public static class WizardryItemTags {
		public static final TagKey<Item> CRYSTALS = createKey("crystals");
		
		public static TagKey<Item> createKey(String name) {
			return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Wizardry.MODID, name));
		}
	}
}
