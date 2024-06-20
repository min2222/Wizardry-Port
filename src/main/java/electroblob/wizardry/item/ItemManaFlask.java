package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class ItemManaFlask extends Item {

	public enum Size {

		SMALL(75, 25, Rarity.COMMON),
		MEDIUM(350, 40, Rarity.COMMON),
		LARGE(1400, 60, Rarity.RARE);

		public int capacity;
		public int useDuration;
		public Rarity rarity;

		Size(int capacity, int useDuration, Rarity rarity){
			this.capacity = capacity;
			this.useDuration = useDuration;
			this.rarity = rarity;
		}
	}

	public final Size size;

	public ItemManaFlask(Size size){
        super(new Item.Properties().tab(WizardryTabs.WIZARDRY).stacksTo(16));
		this.size = size;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return size.rarity;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + Wizardry.MODID + ":mana_flask.desc", size.capacity);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack){
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack){
		return size.useDuration;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack flask = player.getItemInHand(hand);

		List<ItemStack> stacks = InventoryUtils.getPrioritisedHotbarAndOffhand(player);
		stacks.addAll(player.getInventory().armor); // player#getArmorInventoryList() only returns an Iterable

		if(stacks.stream().anyMatch(s -> s.getItem() instanceof IManaStoringItem && !((IManaStoringItem)s.getItem()).isManaFull(s))){

			if(player.getAbilities().instabuild){
				findAndChargeItem(flask, player);
			}else{
				player.startUsingItem(hand);
			}

			return new InteractionResultHolder<>(InteractionResult.SUCCESS, flask);

		}else{
			return new InteractionResultHolder<>(InteractionResult.FAIL, flask);
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count){
		if(player.level.isClientSide){
			float f = count/(float)getUseDuration(stack);
			Vec3 pos = player.getEyePosition(0).subtract(0, 0.2, 0).add(player.getLookAngle().scale(0.6));
			Vec3 delta = new Vec3(0, 0.2 * f, 0).xRot(count * 0.5f).yRot((float)Math.toRadians(90 - player.yHeadRot));
			ParticleBuilder.create(Type.DUST).pos(pos.add(delta)).vel(delta.scale(0.2)).time(12 + player.level.random.nextInt(6))
					.clr(1, 1, 0.65f).fade(0.7f, 0, 1).spawn(player.level);
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity){

		if(entity instanceof Player){
			findAndChargeItem(stack, (Player)entity);
		}

		return stack;
	}

	private void findAndChargeItem(ItemStack stack, Player player){

		List<ItemStack> stacks = InventoryUtils.getPrioritisedHotbarAndOffhand(player);
		stacks.addAll(player.getInventory().armor); // player#getArmorInventoryList() only returns an Iterable

		// Find the chargeable item with the least mana
		ItemStack toCharge = stacks.stream()
				.filter(s -> s.getItem() instanceof IManaStoringItem && !((IManaStoringItem)s.getItem()).isManaFull(s))
				.min(Comparator.comparingDouble(s -> ((IManaStoringItem)s.getItem()).getFullness(s))).orElse(null);

		if(toCharge != null){

			((IManaStoringItem)toCharge.getItem()).rechargeMana(toCharge, size.capacity);

			EntityUtils.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_USE, 1, 1);
			EntityUtils.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_RECHARGE, 0.7f, 1.1f);

			if(!player.isCreative()) stack.shrink(1);
			player.getCooldowns().addCooldown(this, 20);
		}
	}

}
