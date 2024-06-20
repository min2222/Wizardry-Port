package electroblob.wizardry.item;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemArmourUpgrade extends Item {

	public ItemArmourUpgrade(){
		super(new Item.Properties().stacksTo(1).tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.EPIC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		Wizardry.proxy.addMultiLineDescription(tooltip, this.getOrCreateDescriptionId() + ".desc");
	}

}
