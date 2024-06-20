package electroblob.wizardry.registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.spell.Spell;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Class responsible for defining and storing all of wizardry's creative tabs. Also handles sorting of the items.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
public final class WizardryTabs {

	private WizardryTabs(){} // No instances!

	public static final CreativeModeTab WIZARDRY = new CreativeTabListed("ebwizardry");
	public static final CreativeModeTab GEAR = new CreativeTabListed("ebwizardrygear");
	public static final CreativeModeTab SPELLS = new CreativeTabSorted("ebwizardryspells",
			
			(stack1, stack2) -> {

	            if((stack1.getItem() instanceof ItemSpellBook && stack2.getItem() instanceof ItemSpellBook)
	                    || (stack1.getItem() instanceof ItemScroll && stack2.getItem() instanceof ItemScroll)){

	                Spell spell1 = Spell.byMetadata(((IMetadata) stack1.getItem()).getMetadata(stack2));
	                Spell spell2 = Spell.byMetadata(((IMetadata) stack2.getItem()).getMetadata(stack2));

	                return spell1.compareTo(spell2);

	            }else if(stack1.getItem() instanceof ItemScroll){
	                return 1;
	            }else if(stack2.getItem() instanceof ItemScroll){
	                return -1;
	            }
	            return 0;
	        },
			
			true);
	
	public static class CreativeTabSorted extends CreativeModeTab {
		
		private ItemStack iconItem;
		private final Comparator<? super ItemStack> sorter;
		private final boolean searchable;
		
		public CreativeTabSorted(String label, Comparator<? super ItemStack> sorter){
			this(label, sorter, false);
		}

		public CreativeTabSorted(String label, Comparator<? super ItemStack> sorter, boolean searchable){
			super(label);
			this.sorter = sorter;
			this.searchable = searchable;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon(){
			return iconItem;
		}
		
		public void setIconItem(ItemStack iconItem){
			this.iconItem = iconItem;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void fillItemList(NonNullList<ItemStack> items){
			super.fillItemList(items);
			items.sort(sorter);
		}

		@Override
		public boolean hasSearchBar(){
			return searchable;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public ResourceLocation getBackgroundImage(){
            return searchable ? new ResourceLocation("textures/gui/container/creative_inventory/tab_" + "item_search.png") : super.getBackgroundImage();
		}
	}
	
	public static class CreativeTabListed extends CreativeTabSorted {

		public final List<Item> order;

		public CreativeTabListed(String label){
			// Can't accomplish this in a single constructor... just a quirk of the java compiler!
			this(label, new ArrayList<>());
		}
		
		// Hey, maybe someone might want to keep a reference to the list, or pass in a different one.
		public CreativeTabListed(String label, List<Item> order){
			
			super(label, (stack1, stack2) -> {
				// Neither stack is in the creative tab
				if(!order.contains(stack1.getItem()) && !order.contains(stack2.getItem())) return 0;
				if(!order.contains(stack1.getItem())) return 1; // Only stack 2 is in the creative tab
				if(!order.contains(stack2.getItem())) return -1; // Only stack 1 is in the creative tab
				// Both stacks are in the creative tab
				return order.indexOf(stack1.getItem()) - order.indexOf(stack2.getItem());
			});
			
			this.order = order;
		}
	}

}
