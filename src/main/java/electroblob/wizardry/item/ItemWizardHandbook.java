package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWizardHandbook extends Item {

	// Yep, I hardcoded my own name into the mod. Don't want people changing it now, do I?
	private static final String AUTHOR = "Electroblob";

	public ItemWizardHandbook(){
		super();
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_handbook.author",
				new Style().setColor(ChatFormatting.GRAY), AUTHOR));
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getHeldItem(hand);
		if (Wizardry.settings.loadHandbook) {
			player.openGui(Wizardry.instance, WizardryGuiHandler.WIZARD_HANDBOOK, world, 0, 0, 0);
		} else if (!level.isClientSide){
			player.sendStatusMessage(Component.translatable("item." + Wizardry.MODID + ":wizard_handbook.disabled"), false);
		}
		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);
	}

}
