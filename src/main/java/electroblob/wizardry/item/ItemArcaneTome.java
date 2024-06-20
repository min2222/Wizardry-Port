package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemArcaneTome extends Item implements IMetadata {

	public ItemArcaneTome(){
        super(new Item.Properties().stacksTo(1).tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list){
		if(tab == WizardryTabs.WIZARDRY){ // Don't use isInCreativeTab here.
			for(int i = 1; i < Tier.values().length; i++){
                ItemStack stack = new ItemStack(this, 1);
                CompoundTag tag = new CompoundTag();
                tag.putInt("Tier", i);
                stack.addTagElement("Tiers", tag);
                list.add(stack);
			}
		}
	}
	
	@Override
	public boolean getHasSubtypes(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getMetadata(ItemStack stack) {
		return stack.getTagElement("Tiers").getInt("Tier");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		switch(this.getMetadata(stack)){
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
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag showAdvanced){

		if(this.getMetadata(stack) < 1){
			return; // If something's up with the metadata it will display a 'generic' tome of arcana with no info
		}

		Tier tier = Tier.values()[this.getMetadata(stack)];
		Tier tier2 = Tier.values()[this.getMetadata(stack) - 1];

		tooltip.add(tier.getDisplayNameWithFormatting());
		Wizardry.proxy.addMultiLineDescription(tooltip, this.getOrCreateDescriptionId() + ".desc", tier2.getDisplayNameWithFormatting(), tier.getDisplayNameWithFormatting());
	}

}
