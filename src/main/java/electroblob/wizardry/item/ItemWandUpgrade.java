package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWandUpgrade extends Item {

	public ItemWandUpgrade(){
		super();
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.UNCOMMON;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}
}
