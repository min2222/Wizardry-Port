package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPurifyingElixir extends Item {

	public ItemPurifyingElixir(){
		this.setMaxStackSize(1);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.RARE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, Level world, LivingEntity entity){

		if(!level.isClientSide){
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

		world.playSound(entity.getX(), entity.getY(), entity.getZ(), WizardrySounds.ITEM_PURIFYING_ELIXIR_DRINK, SoundSource.PLAYERS, 1, 1, false);

		if(entity instanceof ServerPlayer){
			ServerPlayer entityplayermp = (ServerPlayer)entity;
			CriteriaTriggers.CONSUME_ITEM.trigger(entityplayermp, stack);
		}

		if(entity instanceof Player && !((Player)entity).capabilities.isCreativeMode){
			stack.shrink(1);
		}

		return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 32;
	}

	@Override
	public UseAnim getItemUseAction(ItemStack stack){
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level worldIn, Player playerIn, InteractionHand handIn){
		playerIn.setActiveHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
}
