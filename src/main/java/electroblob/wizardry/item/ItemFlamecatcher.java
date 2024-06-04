package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityFlamecatcherArrow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Flamecatcher;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Rarity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ItemFlamecatcher extends ItemBow implements IConjuredItem {

	public static final float DRAW_TIME = 10;

	public ItemFlamecatcher(){
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
					return itemstack.getItem() == ItemFlamecatcher.this
							? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / DRAW_TIME : 0;
				}
			}
		});
		this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter(){
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level worldIn, @Nullable LivingEntity entityIn){
				return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1 : 0;
			}
		});
		addAnimationPropertyOverrides();
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.EPIC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	// Why does this still exist? Item models deal with this now, right?
	public boolean isFull3D(){
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.flamecatcher);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged)
			// This code should only run on the client side, so using Minecraft is ok.
				// Why the heck was this here?
					//&& !net.minecraft.client.Minecraft.getMinecraft().player.isHandActive())
				return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	// Copied fixes from ItemWand made possible by recently-added Forge hooks

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return true;
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return false;
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
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
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getHeldItem(hand);

		int shotsLeft = stack.getTag().getInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY);
		if(shotsLeft == 0) return InteractionResultHolder.newResult(InteractionResult.PASS, stack);

		InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(stack, world, player, hand,
				true);

		if(ret != null) return ret;

		player.setActiveHand(hand);

		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);

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

			Player player = (Player)entity;

			int charge = this.getMaxItemUseDuration(stack) - timeLeft;
			charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, world, (Player)entity, charge, true);
			if(charge < 0) return;

			float velocity = (float)charge / DRAW_TIME;
			velocity = (velocity * velocity + velocity * 2) / 3;

			if(velocity > 1) velocity = 1;

			if((double)velocity >= 0.1D){

				if(stack.getTag() != null){
					int shotsLeft = stack.getTag().getInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY) - 1;
					stack.getTag().putInt(Flamecatcher.SHOTS_REMAINING_NBT_KEY, shotsLeft);
					if(shotsLeft == 0 && !level.isClientSide){
						stack.setItemDamage(getMaxDamage(stack) - getAnimationFrames());
					}
				}

				if(!level.isClientSide){
					EntityFlamecatcherArrow arrow = new EntityFlamecatcherArrow(world);
					arrow.aim(player, EntityFlamecatcherArrow.SPEED * velocity);
					world.spawnEntity(arrow);
				}

				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						WizardrySounds.ITEM_FLAMECATCHER_SHOOT, WizardrySounds.SPELLS, 1, 1);

				world.playSound(null, player.getX(), player.getY(), player.getZ(),
						WizardrySounds.ITEM_FLAMECATCHER_FLAME, WizardrySounds.SPELLS, 1, 1);
			}
		}
	}

}
