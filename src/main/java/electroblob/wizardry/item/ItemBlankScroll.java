package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemBlankScroll extends Item implements IWorkbenchItem {

	public ItemBlankScroll(){
        super(new Item.Properties().tab(WizardryTabs.WIZARDRY));
		this.addPropertyOverride(new ResourceLocation("festive"), (s, w, e) -> Wizardry.tisTheSeason ? 1 : 0);
	}

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return 1;
	}

	@Override
	public boolean showTooltip(ItemStack stack){
		return false;
	}

	@Override
	public boolean onApplyButtonPressed(Player player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		if(!spellBooks[0].getItem().isEmpty() && !crystals.getItem().isEmpty()){
			
			Spell spell = Spell.byMetadata(((IMetadata) spellBooks[0].getItem().getItem()).getMetadata(spellBooks[0].getItem()));
			WizardData data = WizardData.get(player);

			// Spells can only be bound to scrolls if the player has already cast them (prevents casting of master
			// spells without getting a master wand)
			// This restriction does not apply in creative mode
			if(spell != Spells.NONE && player.isCreative() || (data != null
					&& data.hasSpellBeenDiscovered(spell)) && spell.isEnabled(SpellProperties.Context.SCROLL)){
				
				int cost = spell.getCost() * centre.getItem().getCount();
				// Continuous spell scrolls require enough mana to cast them for the duration defined in ItemScroll.
				if(spell.isContinuous) cost *= ItemScroll.CASTING_TIME / 20;

				int manaPerItem = Constants.MANA_PER_CRYSTAL;
				if(crystals.getItem().getItem() == WizardryItems.CRYSTAL_SHARD.get()) manaPerItem = Constants.MANA_PER_SHARD;
				if(crystals.getItem().getItem() == WizardryItems.GRAND_CRYSTAL.get()) manaPerItem = Constants.GRAND_CRYSTAL_MANA;
				
				if(crystals.getItem().getCount() * manaPerItem > cost){
					// Rounds up to the nearest whole crystal
					crystals.remove(Mth.ceil((float)cost / manaPerItem));
                    ItemStack stack = new ItemStack(WizardryItems.SCROLL.get(), centre.getItem().getCount());
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("Spell", spell.metadata());
                    stack.addTagElement("Spells", tag);
					return true;
				}
				
			}
		}
		
		return false;
	}

}
