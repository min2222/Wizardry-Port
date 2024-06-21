package electroblob.wizardry.misc;

import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Custom version of {@link MerchantOffers} which allows wildcard recipes (i.e. trades which accept items with any
 * damage value). Function is otherwise identical. For some reason this feature was removed in 1.11.
 * @author Electroblob
 * @since Wizardry 4.1 */
@SuppressWarnings("serial")
public class WildcardTradeList extends MerchantOffers {

	private int currentIndex;

	public WildcardTradeList(){
		super();
	}
	
	public WildcardTradeList(CompoundTag tag){
		super(tag);
	}

	/** Returns the current recipe  */
	public MerchantOffer getCurrentRecipe(){
		return get(currentIndex); // Allows events to access the selected recipe without reflection
	}

	@Override
    public MerchantOffer getRecipeFor(ItemStack offer1, ItemStack offer2, int index){

		currentIndex = index; // Update the index
		
        if(index > 0 && index < this.size()){
        	
        	MerchantOffer merchantrecipe1 = this.get(index);
            return !this.areItemStacksExactlyEqual(offer1, merchantrecipe1.getBaseCostA()) || (!offer2.isEmpty() || !merchantrecipe1.getCostB().isEmpty()) && (merchantrecipe1.getCostB().isEmpty() || !this.areItemStacksExactlyEqual(offer2, merchantrecipe1.getCostB())) || offer1.getCount() < merchantrecipe1.getBaseCostA().getCount() || !merchantrecipe1.getCostB().isEmpty() && offer2.getCount() < merchantrecipe1.getCostB().getCount() ? null : merchantrecipe1;
        
        }else{
        	
            for(int i = 0; i < this.size(); ++i){
            	
            	MerchantOffer merchantrecipe = this.get(i);

                if (this.areItemStacksExactlyEqual(offer1, merchantrecipe.getBaseCostA()) && offer1.getCount() >= merchantrecipe.getBaseCostA().getCount() && (merchantrecipe.getCostB().isEmpty() && offer2.isEmpty() || !merchantrecipe.getCostB().isEmpty() && this.areItemStacksExactlyEqual(offer2, merchantrecipe.getCostB()) && offer2.getCount() >= merchantrecipe.getCostB().getCount())){
                    return merchantrecipe;
                }
            }
            
            return null;
        }
    }

    private boolean areItemStacksExactlyEqual(ItemStack stack1, ItemStack stack2){
    	// Added to allow wildcards
    	if((((IMetadata) stack1.getItem()).getMetadata(stack1) == Short.MAX_VALUE || ((IMetadata) stack2.getItem()).getMetadata(stack2) == Short.MAX_VALUE)
    			// Can't use ItemStack.areItemsEqualIgnoreDurability because that only works for items with durability, not subtypes.
    			&& stack1.getItem() == stack2.getItem()) return true;
    	
        return ItemStack.isSame(stack1, stack2) && (!stack2.hasTag() || stack1.hasTag() && NbtUtils.compareNbt(stack2.getTag(), stack1.getTag(), false));
    }

	@Override
	public void writeToStream(FriendlyByteBuf buffer){

		buffer.writeByte((byte)(this.size() & 255));

		// Trick the client into thinking this is a normal item
		for(MerchantOffer merchantrecipe : this){

			ItemStack itemToBuy = merchantrecipe.getBaseCostA();
			if(((IMetadata) itemToBuy.getItem()).getMetadata(itemToBuy) == Short.MAX_VALUE) itemToBuy = InventoryUtils.copyWithMeta(itemToBuy, 0);
			buffer.writeItem(itemToBuy);

			ItemStack itemToSell = merchantrecipe.getResult();
			if(((IMetadata) itemToSell.getItem()).getMetadata(itemToSell) == Short.MAX_VALUE) itemToSell = InventoryUtils.copyWithMeta(itemToSell, 0);
			buffer.writeItem(itemToSell);

			ItemStack secondItemToBuy = merchantrecipe.getCostB();
			buffer.writeBoolean(!secondItemToBuy.isEmpty());

			if(!secondItemToBuy.isEmpty()){
				if(((IMetadata) secondItemToBuy.getItem()).getMetadata(secondItemToBuy) == Short.MAX_VALUE) secondItemToBuy = InventoryUtils.copyWithMeta(secondItemToBuy, 0);
				buffer.writeItem(secondItemToBuy);
			}

			buffer.writeBoolean(merchantrecipe.isOutOfStock());
			buffer.writeInt(merchantrecipe.getUses());
			buffer.writeInt(merchantrecipe.getMaxUses());
		}
	}
	
}
