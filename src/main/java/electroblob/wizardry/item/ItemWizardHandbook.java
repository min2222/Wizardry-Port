package electroblob.wizardry.item;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemWizardHandbook extends Item {

	// Yep, I hardcoded my own name into the mod. Don't want people changing it now, do I?
	private static final String AUTHOR = "Electroblob";

	public ItemWizardHandbook(){
        super(new Item.Properties().stacksTo(1).tab(WizardryTabs.WIZARDRY));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_handbook.author",
				Style.EMPTY.withColor(ChatFormatting.GRAY), AUTHOR));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		if (Wizardry.settings.loadHandbook) {
			player.openGui(Wizardry.instance, WizardryGuiHandler.WIZARD_HANDBOOK, world, 0, 0, 0);
		} else if (!world.isClientSide){
			player.displayClientMessage(Component.translatable("item." + Wizardry.MODID + ":wizard_handbook.disabled"), false);
		}
		return InteractionResultHolder.success(stack);
	}

}
