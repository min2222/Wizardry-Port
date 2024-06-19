package electroblob.wizardry.legacy;

import net.minecraft.world.item.ItemStack;

public interface IMetadata {
	
	boolean getHasSubtypes(ItemStack stack);
	
	int getMetadata(ItemStack stack);
}
