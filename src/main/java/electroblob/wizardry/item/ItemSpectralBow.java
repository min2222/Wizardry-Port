package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSpectralBow extends BowItem implements IConjuredItem {

	public ItemSpectralBow(){
        super(new Item.Properties().durability(1200).setNoRepair());
        ItemProperties.register(this, new ResourceLocation("pull"), (stack, p_174636_, entityIn, p_174638_) -> {
			if(entityIn == null){
				return 0.0F;
			}else{
				ItemStack itemstack = entityIn.getUseItem();
				return itemstack.getItem() == ItemSpectralBow.this
						? (float)(stack.getUseDuration() - entityIn.getUseItemRemainingTicks()) / 20.0F
						: 0.0F;
			}
		});
		ItemProperties.register(this, new ResourceLocation("pulling"), (stack, p_174636_, entityIn, p_174638_) -> {
			return entityIn != null && entityIn.isUsingItem() && entityIn.getUseItem() == stack ? 1.0F
					: 0.0F;
		});
		addAnimationPropertyOverrides();
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.CONJURE_BOW);
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

		InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.startUsingItem(hand);

		return InteractionResultHolder.success(stack);

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

			Player entityplayer = (Player)entity;

			int i = this.getUseDuration(stack) - timeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (Player)entity, i, true);
			if(i < 0) return;

			float f = getPowerForTime(i);

			if((double)f >= 0.1D){

				if(!world.isClientSide){

					EntityConjuredArrow entityarrow = new EntityConjuredArrow(world, entityplayer);
					entityarrow.shootFromRotation(entityplayer, entityplayer.getXRot(), entityplayer.getYRot(), 0.0F,
							f * 3.0F, 1.0F);

					if(f == 1.0F){
						entityarrow.setCritArrow(true);
					}

					int j = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);

					if(j > 0){
						entityarrow.setBaseDamage(entityarrow.getBaseDamage() + (double)j * 0.5D + 0.5D);
					}

					int k = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);

					if(k > 0){
						entityarrow.setKnockback(k);
					}

					if(stack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0){
						entityarrow.setSecondsOnFire(100);
					}

					entityarrow.pickup = Arrow.Pickup.DISALLOWED;

					entityarrow.setBaseDamage(entityarrow.getBaseDamage() * IConjuredItem.getDamageMultiplier(stack));

					world.addFreshEntity(entityarrow);
				}

				world.playSound(null, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(),
						SoundEvents.ARROW_SHOOT, SoundSource.NEUTRAL, 1.0F,
						1.0F / (world.random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

				entityplayer.awardStat(Stats.ITEM_USED.get(this));
			}
		}
	}

}
