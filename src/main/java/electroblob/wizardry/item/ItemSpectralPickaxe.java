package electroblob.wizardry.item;

import javax.annotation.Nullable;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSpectralPickaxe extends PickaxeItem implements IConjuredItem {

	private Rarity rarity = Rarity.COMMON;

	public ItemSpectralPickaxe(Tier material, int damage, float speed){
		super(material, damage, speed, new Item.Properties().durability(1200).setNoRepair());
		addAnimationPropertyOverrides();
	}

	public Item setRarity(Rarity rarity){
		this.rarity = rarity;
		return this;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return rarity;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.CONJURE_PICKAXE);
	}

	@Override
	public int getBarColor(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// tick() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getDamageValue();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setDamageValue(damage + 1);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state){
		float speed = super.getDestroySpeed(stack, state);
		return speed > 1 ? speed * IConjuredItem.getDamageMultiplier(stack) : speed;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable Player player, @Nullable BlockState blockState){
		// Reuses the standard bonus amplifier calculation from SpellBuff to increase the mining level at advanced and master tier
		return super.getHarvestLevel(stack, toolClass, player, blockState) + (int)((IConjuredItem.getDamageMultiplier(stack) - 1) / 0.4);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public boolean isRepairable(ItemStack stack){
		return false;
	}

	@Override
	public int getEnchantmentValue(){
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player){
		return false;
	}
}
