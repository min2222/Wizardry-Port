package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemArcaneTome extends Item {

	public ItemArcaneTome(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> list){
		if(tab == WizardryTabs.WIZARDRY){ // Don't use isInCreativeTab here.
			for(int i = 1; i < Tier.values().length; i++){
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		switch(this.getDamage(stack)){
		case 1:
			return Rarity.UNCOMMON;
		case 2:
			return Rarity.RARE;
		case 3:
			return Rarity.EPIC;
		}
		return Rarity.COMMON;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag showAdvanced){

		if(stack.getItemDamage() < 1){
			return; // If something's up with the metadata it will display a 'generic' tome of arcana with no info
		}

		Tier tier = Tier.values()[stack.getItemDamage()];
		Tier tier2 = Tier.values()[stack.getItemDamage() - 1];

		tooltip.add(tier.getDisplayNameWithFormatting());
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc",
				tier2.getDisplayNameWithFormatting() + "\u00A77", tier.getDisplayNameWithFormatting() + "\u00A77");
	}

}
