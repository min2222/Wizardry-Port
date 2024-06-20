package electroblob.wizardry.item;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.InventoryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

public class ItemIdentificationScroll extends Item {

	public ItemIdentificationScroll(){
        super(new Item.Properties().tab(WizardryTabs.WIZARDRY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.UNCOMMON;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, this.getOrCreateDescriptionId() + ".desc");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		if(WizardData.get(player) != null){

			WizardData data = WizardData.get(player);

			for(ItemStack stack1 : InventoryUtils.getPrioritisedHotbarAndOffhand(player)){

				if(!stack1.isEmpty()){
					Spell spell = Spell.byMetadata(((IMetadata) stack1.getItem()).getMetadata(stack1));
					if((stack1.getItem() instanceof ItemSpellBook || stack1.getItem() instanceof ItemScroll)
							&& !data.hasSpellBeenDiscovered(spell)){

						if(!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(player, spell,
								DiscoverSpellEvent.Source.IDENTIFICATION_SCROLL))){
							// Identification scrolls give the chat readout in creative mode, otherwise it looks like
							// nothing happens!
							data.discoverSpell(spell);
							player.playSound(WizardrySounds.MISC_DISCOVER_SPELL, 1.25f, 1);
							if(!player.isCreative()) stack.shrink(1);
							if(!world.isClientSide) player.sendSystemMessage(Component.translatable("spell.discover",
									spell.getNameForTranslationFormatted()));

							return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
						}
					}
				}
			}
			// If it found nothing to identify, it says so!
			if(!world.isClientSide) player.sendSystemMessage(
					Component.translatable("item." + Wizardry.MODID + ":identification_scroll.nothing_to_identify"));
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

}
