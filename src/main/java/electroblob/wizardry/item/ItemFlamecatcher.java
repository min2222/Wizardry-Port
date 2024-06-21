package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntityFlamecatcherArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Flamecatcher;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class ItemFlamecatcher extends BowItem implements IConjuredItem {

	public static final float DRAW_TIME = 10;

	public ItemFlamecatcher(){
        super(new Item.Properties().durability(1200).setNoRepair());
		ItemProperties.register(this, new ResourceLocation("pull"), (stack, p_174636_, entityIn, p_174638_) -> {
			if(entityIn == null){
				return 0.0F;
			}else{
				ItemStack itemstack = entityIn.getUseItem();
				return itemstack.getItem() == ItemFlamecatcher.this
						? (float)(stack.getUseDuration() - entityIn.getUseItemRemainingTicks()) / DRAW_TIME : 0;
			}
		});
		ItemProperties.register(this, new ResourceLocation("pulling"), (stack, p_174636_, entityIn, p_174638_) -> {
			return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1 : 0;
		});
		addAnimationPropertyOverrides();
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.EPIC;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.FLAMECATCHER);
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
			if(oldStack.getItem() == newStack.getItem() && !slotChanged)
			// This code should only run on the client side, so using Minecraft is ok.
				// Why the heck was this here?
					//&& !net.minecraft.client.Minecraft.getInstance().player.isHandActive())
				return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	// Copied fixes from ItemWand made possible by recently-added Forge hooks

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.isSameIgnoreDurability(oldStack, newStack)) return true;
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.isSameIgnoreDurability(oldStack, newStack)) return false;
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getDamageValue();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setDamageValue(damage + 1);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		int shotsLeft = stack.getTag().getInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY);
		if(shotsLeft == 0) return InteractionResultHolder.pass(stack);

		InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.startUsingItem(hand);

		return InteractionResultHolder.success(stack);

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

//	@Override
//	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count){
//		// player.getItemInUseMaxCount() is named incorrectly; you only have to look at the method to see what it really
//		// does.
//		if(stack.getItemDamage() + player.getItemInUseMaxCount() > stack.getMaxDamage())
//			player.replaceItemInInventory(player.getActiveHand() == EnumHand.MAIN_HAND ? 98 : 99, ItemStack.EMPTY);
//	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft){
		// Decreases the timer by the amount it should have been decreased while the bow was in use.
		if(!world.isClientSide) stack.setDamageValue(stack.getDamageValue() + (this.getUseDuration(stack) - timeLeft));

		if(entity instanceof Player){

			Player player = (Player)entity;

			int charge = this.getUseDuration(stack) - timeLeft;
			charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (Player)entity, charge, true);
			if(charge < 0) return;

			float velocity = (float)charge / DRAW_TIME;
			velocity = (velocity * velocity + velocity * 2) / 3;

			if(velocity > 1) velocity = 1;

			if((double)velocity >= 0.1D){

				if(stack.getTag() != null){
					int shotsLeft = stack.getTag().getInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY) - 1;
					stack.getTag().putInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY, shotsLeft);
					if(shotsLeft == 0 && !world.isClientSide){
						stack.setDamageValue(getMaxDamage(stack) - getAnimationFrames());
					}
				}

				if(!world.isClientSide){
					EntityFlamecatcherArrow arrow = new EntityFlamecatcherArrow(world);
					arrow.aim(player, EntityFlamecatcherArrow.SPEED * velocity);
					world.addFreshEntity(arrow);
				}

				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						WizardrySounds.ITEM_FLAMECATCHER_SHOOT, WizardrySounds.SPELLS, 1, 1);

				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						WizardrySounds.ITEM_FLAMECATCHER_FLAME, WizardrySounds.SPELLS, 1, 1);
			}
		}
	}

}
