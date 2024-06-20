package electroblob.wizardry.item;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSpellBook extends Item implements IMetadata{

	private static final Map<Tier, ResourceLocation> guiTextures = ImmutableMap.of(
			Tier.NOVICE, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_novice.png"),
			Tier.APPRENTICE, 	new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_apprentice.png"),
			Tier.ADVANCED, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_advanced.png"),
			Tier.MASTER, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_master.png"));

	public ItemSpellBook(){
        super(new Item.Properties().stacksTo(16).tab(WizardryTabs.SPELLS));
		this.addPropertyOverride(new ResourceLocation("festive"), (s, w, e) -> Wizardry.tisTheSeason ? 1 : 0);
	}
	
	@Override
	public boolean getHasSubtypes(ItemStack stack) {
		return true;
	}
	
    @Override
    public int getMetadata(ItemStack stack) {
        return stack.getTagElement("Spells") != null ? stack.getTagElement("Spells").getInt("Spell") : 0;
    }

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list){

		if(tab == WizardryTabs.SPELLS){

			List<Spell> spells = Spell.getAllSpells();
			spells.removeIf(s -> !s.applicableForItem(this));

			for(Spell spell : spells){
                ItemStack stack = new ItemStack(this, 1);
                CompoundTag tag = new CompoundTag();
                tag.putInt("Spell", spell.metadata());
                stack.addTagElement("Spells", tag);
                list.add(stack);
			}
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		player.openGui(Wizardry.instance, WizardryGuiHandler.SPELL_BOOK, world, 0, 0, 0);
		return InteractionResultHolder.newResult(InteractionResult.SUCCESS, stack);
	}

	// This is accessed during loading (before we even get to the main menu) for search tree population
	// Obviously the world is always null at that point, because no world objects exist! However, outside of a world
	// there are no guarantees as to spell metadata order so we just have to give up (and we can't account for discovery)
	// TODO: Search trees seem to get reloaded when the mappings change so in theory this should work ok, why doesn't it?
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> tooltip, TooltipFlag advanced){

		if(world == null) world = Wizardry.proxy.getTheWorld(); // But... I need the world!

		// Tooltip is left blank for wizards buying generic spell books.
		if(world != null && itemstack.getItemDamage() != OreDictionary.WILDCARD_VALUE){

			Spell spell = Spell.byMetadata(itemstack.getItemDamage());

			boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(spell, itemstack);

			// Element colour is not given for undiscovered spells
			tooltip.add(discovered ? "\u00A77" + spell.getDisplayNameWithFormatting()
					: "#\u00A79" + SpellGlyphData.getGlyphName(spell, world));

			tooltip.add(spell.getTier().getDisplayNameWithFormatting());

			Player player = Wizardry.proxy.getThePlayer();

			// If the spell should *appear* discovered but isn't *actually* discovered, show a 'new spell' message
			// A bit annoying to check this again but it's the easiest way
			if(Wizardry.settings.discoveryMode && !player.isCreative() && discovered && WizardData.get(player) != null && !WizardData.get(player).hasSpellBeenDiscovered(spell)){
				tooltip.add(Wizardry.proxy.translate("item." + this.getRegistryName() + ".new", new Style().setColor(ChatFormatting.LIGHT_PURPLE)));
			}

			// Advanced tooltips display more information, mainly for searching purposes in creative
			if(discovered && advanced.isAdvanced()){ // No cheating!
				tooltip.add(spell.getElement().getDisplayName());
				tooltip.add(spell.getType().getDisplayName());
			}
			// Advanced tooltips displays the source mod's name if the spell is not from Wizardry
			if (advanced.isAdvanced() && this.getRegistryName().toString().equals(Wizardry.MODID + ":spell_book") && !spell.getRegistryName().getNamespace().equals(Wizardry.MODID)) {
				String modId = spell.getRegistryName().getNamespace();
				String name = new Style().setColor(ChatFormatting.BLUE).setItalic(true).getFormattingCode() +
						Loader.instance().getIndexedModList().get(modId).getMetadata().name;
				tooltip.add(name);
			}
		}
	}
	
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public @Nullable Font getFont(ItemStack stack, FontContext context) {
				return Wizardry.proxy.getFontRenderer(stack);
			}
		});
	}

	/**
	 * Returns the GUI texture to be used when this spell book is opened.
	 * @param spell The spell for the book being opened.
	 */
	public ResourceLocation getGuiTexture(Spell spell){
		return guiTextures.get(spell.getTier());
	}

}
