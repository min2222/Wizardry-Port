package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

public class ItemFirebomb extends Item {

	public ItemFirebomb(){
		setMaxStackSize(16);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(!player.isCreative()){
			stack.shrink(1);
		}

		player.playSound(WizardrySounds.ENTITY_FIREBOMB_THROW, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		player.getCooldownTracker().setCooldown(this, 20);

		if(!world.isRemote){
			EntityFirebomb firebomb = new EntityFirebomb(world);
			firebomb.aim(player, 1);
			world.spawnEntity(firebomb);
		}

		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);
	}
	
}