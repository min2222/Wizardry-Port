package electroblob.wizardry.item;

import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemSparkBomb extends Item {

	public ItemSparkBomb(){
        super(new Item.Properties().stacksTo(16).tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		if(!player.isCreative()){
			stack.shrink(1);
		}

		player.playSound(WizardrySounds.ENTITY_SPARK_BOMB_THROW, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

		player.getCooldowns().addCooldown(this, 20);

		if(!world.isClientSide){
			EntitySparkBomb sparkBomb = new EntitySparkBomb(world);
			sparkBomb.aim(player, 1);
			world.addFreshEntity(sparkBomb);
		}

		return InteractionResultHolder.success(stack);
	}
	
}