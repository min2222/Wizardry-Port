package electroblob.wizardry.item;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemPurifyingElixir extends Item {

	public ItemPurifyingElixir(){
        super(new Item.Properties().stacksTo(1).tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.RARE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, this.getOrCreateDescriptionId() + ".desc");
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity){

		if(!world.isClientSide){
			entity.curePotionEffects(stack);
		}else{

			ParticleBuilder.spawnHealParticles(world, entity);

			for(int i = 0; i < 20; i++){
				double x = entity.getX() + world.random.nextDouble() * 2 - 1;
				double y = entity.getY() + entity.getEyeHeight() - 0.5 + world.random.nextDouble();
				double z = entity.getZ() + world.random.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.14, 0).clr(0x0f001b)
						.time(20 + world.random.nextInt(12)).spawn(world);
				ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0x0f001b).spawn(world);
			}
		}

		world.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), WizardrySounds.ITEM_PURIFYING_ELIXIR_DRINK, SoundSource.PLAYERS, 1, 1, false);

		if(entity instanceof ServerPlayer){
			ServerPlayer entityplayermp = (ServerPlayer)entity;
			CriteriaTriggers.CONSUME_ITEM.trigger(entityplayermp, stack);
		}

		if(entity instanceof Player && !((Player)entity).getAbilities().instabuild){
			stack.shrink(1);
		}

		return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
	}

	@Override
	public int getUseDuration(ItemStack stack){
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack){
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn){
		playerIn.startUsingItem(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}
}
