package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

public class ItemSmokeBomb extends Item {

	public ItemSmokeBomb(){
		setMaxStackSize(16);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		if(!player.isCreative()){
			stack.shrink(1);
		}

		player.playSound(WizardrySounds.ENTITY_SMOKE_BOMB_THROW, 0.5F, 0.4F / (itemrandom.nextFloat() * 0.4F + 0.8F));

		player.getCooldowns().addCooldown(this, 20);

		if(!level.isClientSide){
			EntitySmokeBomb smokebomb = new EntitySmokeBomb(world);
			smokebomb.aim(player, 1);
			world.addFreshEntity(smokebomb);
		}

		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);
	}

}