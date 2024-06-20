package electroblob.wizardry.item;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSpectralBow extends BowItem implements IConjuredItem {

	public ItemSpectralBow(){
		super();
		setMaxDamage(1200);
		setNoRepair();
		setCreativeTab(null);
		this.addPropertyOverride(new ResourceLocation("pull"), new IItemPropertyGetter(){
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn){
				if(entityIn == null){
					return 0.0F;
				}else{
					ItemStack itemstack = entityIn.getActiveItemStack();
					return itemstack.getItem() == ItemSpectralBow.this
							? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 20.0F
							: 0.0F;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter(){
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn){
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F
						: 0.0F;
			}
		});
		addAnimationPropertyOverrides();
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.conjure_bow);
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
	public void tick(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setItemDamage(damage + 1);
	}

	// The following two methods re-route the displayed durability through the proxies in order to override the pausing
	// of the item timer when the bow is being pulled.

	@Override
	public double getDurabilityForDisplay(ItemStack stack){
		return Wizardry.proxy.getConjuredBowDurability(stack);
	}

	public double getDefaultDurabilityForDisplay(ItemStack stack){
		return super.getDurabilityForDisplay(stack);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, EnumHand hand){

		ItemStack stack = player.getItemInHand(hand);

		InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.setActiveHand(hand);

		return InteractionResultHolder.newResult(EnumActionResult.SUCCESS, stack);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack stack2){
		return false;
	}

	@Override
	public int getItemEnchantability(){
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
	public void onPlayerStoppedUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft){
		// Decreases the timer by the amount it should have been decreased while the bow was in use.
		if(!level.isClientSide) stack.setItemDamage(stack.getItemDamage() + (this.getMaxItemUseDuration(stack) - timeLeft));

		if(entity instanceof Player){

			Player entityplayer = (Player)entity;

			int i = this.getMaxItemUseDuration(stack) - timeLeft;
			i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (Player)entity, i, true);
			if(i < 0) return;

			float f = getArrowVelocity(i);

			if((double)f >= 0.1D){

				if(!level.isClientSide){

					EntityConjuredArrow entityarrow = new EntityConjuredArrow(world, entityplayer);
					entityarrow.shoot(entityplayer, entityplayer.rotationPitch, entityplayer.rotationYaw, 0.0F,
							f * 3.0F, 1.0F);

					if(f == 1.0F){
						entityarrow.setIsCritical(true);
					}

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);

					if(j > 0){
						entityarrow.setDamage(entityarrow.getDamage() + (double)j * 0.5D + 0.5D);
					}

					int k = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);

					if(k > 0){
						entityarrow.setKnockbackStrength(k);
					}

					if(EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0){
						entityarrow.setSecondsOnFire(100);
					}

					entityarrow.pickupStatus = Arrow.PickupStatus.DISALLOWED;

					entityarrow.setDamage(entityarrow.getDamage() * IConjuredItem.getDamageMultiplier(stack));

					world.addFreshEntity(entityarrow);
				}

				world.playSound(null, entityplayer.getX(), entityplayer.getY(), entityplayer.getZ(),
						SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F,
						1.0F / (itemrandom.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

				entityplayer.addStat(StatList.getObjectUseStats(this));
			}
		}
	}

}
