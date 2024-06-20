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

public class ItemWandUpgrade extends Item {

	public ItemWandUpgrade(){
        super(new Item.Properties().tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.UNCOMMON;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, this.getOrCreateDescriptionId() + ".desc");
	}
}
