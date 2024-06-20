package electroblob.wizardry.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;

public class ItemScroll extends Item implements ISpellCastingItem, IWorkbenchItem, IMetadata {
	
	/** The maximum number of ticks a continuous spell scroll can be cast for (by holding the use item button). */
	public static final int CASTING_TIME = 120;

	public ItemScroll(){
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
	public Spell getCurrentSpell(ItemStack stack){
		return Spell.byMetadata(getMetadata(stack));
	}

	@Override
	public boolean showSpellHUD(Player player, ItemStack stack){
		return false;
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
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	// Item's version of this method is, quite frankly, an abomination. Why is a deprecated method being used as such
	// an integral part of the code? And what's the point in getUnlocalisedNameInefficiently?
	public Component getName(ItemStack stack){

		/* Ok, so this method can be called from either the client or the server side. Obviously, on the client the
		 * spell name is either translated or obfuscated, then it is put into the item name as part of that translation.
		 * On the server side, however, there's a problem: on the one hand, the spell name shouldn't be obfuscated in
		 * case the server wants to do something with it, and in that case returning world-specific gobbledegook is not
		 * particularly helpful. On the other hand, something might happen that causes this method to be called on the
		 * server side, but the result to then be sent to the client, which means broken discovery system. Simply put, I
		 * can't predict that, and it's not my job to cater for other people's incorrect usage of code, especially when
		 * that might compromise some perfectly reasonable use (think Bibliocraft's 'best guess' book detection). */
		return Wizardry.proxy.getScrollDisplayName(stack);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> tooltip, TooltipFlag advanced){

		if(world != null){

			Spell spell = Spell.byMetadata(getMetadata(itemstack));

			boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(spell, itemstack);

			// Advanced tooltips display more information, mainly for searching purposes in creative
			if(discovered && advanced.isAdvanced()){ // No cheating!
				tooltip.add(spell.getTier().getDisplayName());
				tooltip.add(spell.getElement().getDisplayName());
				tooltip.add(spell.getType().getDisplayName());
			}
			// Advanced tooltips displays the source mod's name if the spell is not from Wizardry
			if (advanced.isAdvanced() && this.getRegistryName().toString().equals(Wizardry.MODID + ":scroll") && !spell.getRegistryName().getNamespace().equals(Wizardry.MODID)) {
				String modId = spell.getRegistryName().getNamespace();
				Component name = Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(true).getFormattingCode() +
						Loader.instance().getIndexedModList().get(modId).getMetadata().name;
				tooltip.add(name);
			}
		}
	}
	
	@Override
	public int getUseDuration(ItemStack stack){
		return CASTING_TIME;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		Spell spell = Spell.byMetadata(getMetadata(stack));
		// By default, scrolls have no modifiers - but with the event system, they could be added.
		SpellModifiers modifiers = new SpellModifiers();

		if(canCast(stack, spell, player, hand, 0, modifiers)){
			// Now we can cast continuous spells with scrolls!
			if(spell.isContinuous){
				if(!player.isUsingItem()){
					player.startUsingItem(hand);
					// Store the modifiers for use each tick (there aren't any by default but there could be, as above)
					if(WizardData.get(player) != null) WizardData.get(player).itemCastingModifiers = modifiers;
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
				}
			}else{
				if(cast(stack, spell, player, hand, 0, modifiers)){
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
				}
			}
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}
	
	// For continuous spells. The count argument actually decrements by 1 each tick.
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity user, int count){

		if(user instanceof Player){

			Player player = (Player)user;

			Spell spell = Spell.byMetadata(getMetadata(stack));
			// By default, scrolls have no modifiers - but with the event system, they could be added.
			SpellModifiers modifiers = new SpellModifiers();
			int castingTick = stack.getUseDuration() - count;

			// Continuous spells (these must check if they can be cast each tick since the mana changes)
			// In theory the spell is always continuous here but just in case it isn't...
			if(spell.isContinuous && canCast(stack, spell, player, player.getUsedItemHand(), castingTick, modifiers)){
				cast(stack, spell, player, player.getUsedItemHand(), castingTick, modifiers);
			}else{
				// Scrolls normally work on the max use duration so this isn't ever reached by wizardry, but if the
				// casting was interrupted by SpellCastEvent.Tick it will be used
				player.stopUsingItem();
			}
		}
	}

	@Override
	public boolean canCast(ItemStack stack, Spell spell, Player caster, InteractionHand hand, int castingTick, SpellModifiers modifiers){
		// Even neater!
		if(castingTick == 0){
			return !MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.SCROLL, spell, caster, modifiers));
		}else{
			return !MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(Source.SCROLL, spell, caster, modifiers, castingTick));
		}
	}

	@Override
	public boolean cast(ItemStack stack, Spell spell, Player caster, InteractionHand hand, int castingTick, SpellModifiers modifiers){

		Level world = caster.level;

		if(world.isClientSide && !spell.isContinuous && spell.requiresPacket()) return false;

		if(spell.cast(world, caster, hand, castingTick, modifiers)){

			if(castingTick == 0) MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.SCROLL, spell, caster, modifiers));

			if(!world.isClientSide){

				// Continuous spells never require packets so don't rely on the requiresPacket method to specify it
				if(!spell.isContinuous && spell.requiresPacket()){
					// Sends a packet to all players in dimension to tell them to spawn particles.
					PacketCastSpell.Message msg = new PacketCastSpell.Message(caster.getId(), hand, spell, modifiers);
					WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> world.dimension()), msg);
				}

				// Scrolls are consumed upon successful use in survival mode
				if(!spell.isContinuous && !caster.isCreative()) stack.shrink(1);

				// Now uses the vanilla cooldown mechanic to prevent spamming of spells
				if(!spell.isContinuous && !caster.isCreative()) caster.getCooldowns().addCooldown(this, spell.getCooldown());
			}

			return true;
		}

		return false;
	}
	
	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeLeft){
		// Casting has stopped before the full time has elapsed
		finishCasting(stack, user, timeLeft);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user){
		// Full casting time has elapsed
		finishCasting(stack, user, 0);
		return stack;
	}

	private void finishCasting(ItemStack stack, LivingEntity user, int timeLeft){

		if(Spell.byMetadata(getMetadata(stack)).isContinuous){
			// Consume scrolls in survival mode
			if(!(user instanceof Player) || !((Player)user).isCreative()) stack.shrink(1);

			Spell spell = Spell.byMetadata(getMetadata(stack));
			SpellModifiers modifiers = new SpellModifiers();
			int castingTick = stack.getUseDuration() - timeLeft;

			MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Finish(Source.SCROLL, spell, user, modifiers, castingTick));
			spell.finishCasting(user.level, user, Double.NaN, Double.NaN, Double.NaN, null, castingTick, modifiers);

			if(user instanceof Player && !((Player)user).isCreative()){
				((Player)user).getCooldowns().addCooldown(this, spell.getCooldown());
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

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return 1; // Stop spell books immediately leaving the workbench when a scroll is enchanted
	}

	@Override
	public boolean canPlace(ItemStack stack){
		return false; // Prevent players putting scrolls back in the workbench
	}

	@Override
	public boolean onApplyButtonPressed(Player player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		return false;
	}

	@Override
	public boolean showTooltip(ItemStack stack){
		return false;
	}
}
