package electroblob.wizardry.item;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.legacy.IMetadata;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryRecipes;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.SpellProperties;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class is (literally) where the magic happens! All of wizardry's wand items are instances of this class. As of
 * wizardry 4.2, it is no longer necessary to extend {@code ItemWand} thanks to {@link ISpellCastingItem}, though
 * extending {@code ItemWand} may still be more appropriate for items using the same casting implementation.
 * <p></p>
 * This class handles spell casting as follows:
 * <p></p>
 * - {@code use} is where non-continuous spells are cast, and it sets the item in use for continuous spells<br>
 * - {@code onUsingTick} does the casting for continuous spells<br>
 * - {@code tick} deals with the cooldowns for the spells<br>
 * <br>
 * See {@link ISpellCastingItem} for more detail on the {@code canCast(...)} and {@code cast(...)} methods.<br>
 * See {@link WandHelper} for everything related to wand NBT.
 *
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public class ItemWand extends Item implements IWorkbenchItem, ISpellCastingItem, IManaStoringItem {
	
	/** The number of spell slots a wand has with no attunement upgrades applied. */
	public static final int BASE_SPELL_SLOTS = 5;

	/** The number of ticks between each time a continuous spell is added to the player's recently-cast spells. */
	private static final int CONTINUOUS_TRACKING_INTERVAL = 20;
	/** The increase in progression for casting spells of the matching element. */
	private static final float ELEMENTAL_PROGRESSION_MODIFIER = 1.2f;
	/** The increase in progression for casting an undiscovered spell (can only happen once per spell for each player). */
	private static final float DISCOVERY_PROGRESSION_MODIFIER = 5f;
	/** The increase in progression for tiers that the player has already reached. */
	private static final float SECOND_TIME_PROGRESSION_MODIFIER = 1.5f;
	/** The fraction of progression lost when all recently-cast spells are the same as the one being cast. */
	private static final float MAX_PROGRESSION_REDUCTION = 0.75f;

	public Tier tier;
	public Element element;

	public ItemWand(Tier tier, Element element){
        super(new Item.Properties().durability(tier.maxCharge).tab(WizardryTabs.GEAR));
		this.tier = tier;
		this.element = element;
		WizardryRecipes.addToManaFlaskCharging(this);
		// TODO: Hook to allow addon devs to have this override apply to their own animations
		addPropertyOverride(new ResourceLocation("pointing"),
				(s, w, e) -> e != null && e.getActiveItemStack() == s
						&& (s.getItemUseAction() == SpellActions.POINT
						|| s.getItemUseAction() == SpellActions.POINT_UP
						|| s.getItemUseAction() == SpellActions.POINT_DOWN
						|| s.getItemUseAction() == SpellActions.GRAPPLE
						|| s.getItemUseAction() == SpellActions.SUMMON) ? 1 : 0);
	}

	@Override
	public Spell getCurrentSpell(ItemStack stack){
		return WandHelper.getCurrentSpell(stack);
	}

	@Override
	public Spell getNextSpell(ItemStack stack){
		return WandHelper.getNextSpell(stack);
	}

	@Override
	public Spell getPreviousSpell(ItemStack stack){
		return WandHelper.getPreviousSpell(stack);
	}

	@Override
	public Spell[] getSpells(ItemStack stack){
		return WandHelper.getSpells(stack);
	}

	@Override
	public void selectNextSpell(ItemStack stack){
		WandHelper.selectNextSpell(stack);
	}

	@Override
	public void selectPreviousSpell(ItemStack stack){
		WandHelper.selectPreviousSpell(stack);
	}

	@Override
	public boolean selectSpell(ItemStack stack, int index){
		return WandHelper.selectSpell(stack, index);
	}

	@Override
	public int getCurrentCooldown(ItemStack stack){
		return WandHelper.getCurrentCooldown(stack);
	}

	@Override
	public int getCurrentMaxCooldown(ItemStack stack){
		return WandHelper.getCurrentMaxCooldown(stack);
	}

	@Override
	public boolean showSpellHUD(Player player, ItemStack stack){
		return true;
	}

	@Override
	public boolean showTooltip(ItemStack stack){
		return true;
	}

	/** Does nothing, use {@link ItemWand#setMana(ItemStack, int)} to modify wand mana. */
	@Override
	public void setDamage(ItemStack stack, int damage){
		// Overridden to do nothing to stop repair things from 'repairing' the mana in a wand
	}

	@Override
	public void setMana(ItemStack stack, int mana){
		// Using super (which can only be done from in here) bypasses the above override
		super.setDamage(stack, getManaCapacity(stack) - mana);
	}

	@Override
	public int getMana(ItemStack stack){
		return getManaCapacity(stack) - getDamage(stack);
	}

	@Override
	public int getManaCapacity(ItemStack stack){
		return this.getMaxDamage(stack);
	}
	
	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public @org.jetbrains.annotations.Nullable Font getFont(ItemStack stack, FontContext context) {
				return Wizardry.proxy.getFontRenderer(stack);
			}
		});
	}

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	@Override
	public boolean isFoil(ItemStack stack){
		return !Wizardry.settings.legacyWandLevelling && this.tier.level < Tier.MASTER.level
				&& WandHelper.getProgression(stack) >= tier.next().getProgression();
	}

	@Override
	public int getBarColor(ItemStack stack){
		return DrawingUtils.mix(0xff8bfe, 0x8e2ee4, (float)getBarColor(stack));
	}

	// Max damage is modifiable with upgrades.
	@Override
	public int getMaxDamage(ItemStack stack){
		// + 0.5f corrects small float errors rounding down
		return (int)(super.getMaxDamage(stack) * (1.0f + Constants.STORAGE_INCREASE_PER_LEVEL
				* WandHelper.getUpgradeLevel(stack, WizardryItems.STORAGE_UPGRADE.get())) + 0.5f);
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn){
		setMana(stack, 0); // Wands are empty when first crafted
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isHeldInMainhand){
		boolean isHeld = isHeldInMainhand || entity instanceof LivingEntity && ItemStack.isSame(stack, ((LivingEntity) entity).getOffhandItem());

		// If Wizardry.settings.wandsMustBeHeldToDecrementCooldown is false, the cooldowns will be decremented.
		// If Wizardry.settings.wandsMustBeHeldToDecrementCooldown is true and isHeld is true, the cooldowns will also be decremented.
		// If Wizardry.settings.wandsMustBeHeldToDecrementCooldown is true and isHeld is false, the cooldowns will not be decremented.
		if (!Wizardry.settings.wandsMustBeHeldToDecrementCooldown || isHeld) {
			WandHelper.decrementCooldowns(stack);
		}

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(!world.isClientSide && !this.isManaFull(stack) && world.getGameTime() % Constants.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			this.rechargeMana(stack, WandHelper.getUpgradeLevel(stack, WizardryItems.CONDENSER_UPGRADE.get()));
		}
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack){

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();

		if(slot == EquipmentSlot.MAINHAND){
			int level = WandHelper.getUpgradeLevel(stack, WizardryItems.MELEE_UPGRADE.get());
			// This check doesn't affect the damage output, but it does stop a blank line from appearing in the tooltip.
			if(level > 0 && !this.isManaEmpty(stack)){
				builder.put(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Melee upgrade modifier", 2 * level, Operation.ADDITION));
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Melee upgrade modifier", -2.4000000953674316D, Operation.ADDITION));
			}
		}

		return builder.build();
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity wielder){

		int level = WandHelper.getUpgradeLevel(stack, WizardryItems.MELEE_UPGRADE.get());
		int mana = this.getMana(stack);

		if(level > 0 && mana > 0) this.consumeMana(stack, level * 4, wielder);

		return true;
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity player){
		return WandHelper.getUpgradeLevel(stack, WizardryItems.MELEE_UPGRADE.get()) == 0;
	}

	// A proper hook was introduced for this in Forge build 14.23.5.2805 - Hallelujah, finally!
	// The discussion about this was quite interesting, see the following:
	// https://github.com/TeamTwilight/twilightforest/blob/1.12.x/src/main/java/twilightforest/item/ItemTFScepterLifeDrain.java
	// https://github.com/MinecraftForge/MinecraftForge/pull/4834
	// Among the things mentioned were that it can be 'fixed' by doing the exact same hacks that I did, and that
	// returning a result of PASS rather than SUCCESS from use also solves the problem (not sure why
	// though, and again it's not a perfect solution)
	// Edit: It seems that the hacky fix in previous versions actually introduced a wand duplication bug... oops

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.isSameIgnoreDurability(oldStack, newStack)) return true;
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.isSameIgnoreDurability(oldStack, newStack)) return false;
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	// Only called client-side
	// This method is always called on the item in oldStack, meaning that oldStack.getItem() == this
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		// This method does some VERY strange things! Despite its name, it also seems to affect the updating of NBT...

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged && oldStack.getItem() instanceof ItemWand
					&& newStack.getItem() instanceof ItemWand
					&& WandHelper.getCurrentSpell(oldStack) == WandHelper.getCurrentSpell(newStack))
				return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemstack){
		return WandHelper.getCurrentSpell(itemstack).action;
	}

	@Override
	public int getUseDuration(ItemStack stack){
		return 72000;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> text, TooltipFlag advanced){

		Player player = net.minecraft.client.Minecraft.getInstance().player;
		if (player == null) { return; }
		// +0.5f is necessary due to the error in the way floats are calculated.
		if(element != null) text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.buff",
				Style.EMPTY.withColor(ChatFormatting.DARK_GRAY),
				(int)((tier.level + 1) * Constants.POTENCY_INCREASE_PER_TIER * 100 + 0.5f), element.getDisplayName()));

		Spell spell = WandHelper.getCurrentSpell(stack);

		boolean discovered = true;
		if(Wizardry.settings.discoveryMode && !player.isCreative() && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.spell", Style.EMPTY.withColor(ChatFormatting.GRAY),
				discovered ? spell.getDisplayNameWithFormatting() : "#" + ChatFormatting.BLUE + SpellGlyphData.getGlyphName(spell, player.level)));

		if(advanced.isAdvanced()){
			// Advanced tooltips for debugging
			text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.mana", Style.EMPTY.withColor(ChatFormatting.BLUE),
					this.getMana(stack), this.getManaCapacity(stack)));

			text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.progression", Style.EMPTY.withColor(ChatFormatting.GRAY),
					WandHelper.getProgression(stack), this.tier.level < Tier.MASTER.level ? tier.next().getProgression() : 0));
		}
	}

	@Override
	public Component getName(ItemStack stack){
		return this.element == null ? super.getName(stack) : ((MutableComponent) super.getName(stack)).withStyle(this.element.getColour());
	}

	// Continuous spells use the onUsingItemTick method instead of this one.
	/* An important thing to note about this method: it is only called on the server and the client of the player
	 * holding the item (I call this client-inconsistency). This means if you spawn particles here they will not show up
	 * on other players' screens. Instead, this must be done via packets. */
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand){

		ItemStack stack = player.getItemInHand(hand);

		// Alternate right-click function; overrides spell casting.
		if(this.selectMinionTarget(player, world)) return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

		Spell spell = WandHelper.getCurrentSpell(stack);
		SpellModifiers modifiers = this.calculateModifiers(stack, player, spell);

		if(canCast(stack, spell, player, hand, 0, modifiers)){
			// Need to account for the modifier since it could be zero even if the original charge-up wasn't
			int chargeup = (int)(spell.getChargeup() * modifiers.get(SpellModifiers.CHARGEUP));

			if(spell.isContinuous || chargeup > 0){
				// Spells that need the mouse to be held (continuous, charge-up or both)
				if(!player.isUsingItem()){
					player.startUsingItem(hand);
					// Store the modifiers for use later
					if(WizardData.get(player) != null) WizardData.get(player).itemCastingModifiers = modifiers;
					if(chargeup > 0 && world.isClientSide) Wizardry.proxy.playChargeupSound(player);
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
				}
			}else{
				// All other (instant) spells
				if(cast(stack, spell, player, hand, 0, modifiers)){
					return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
				}
			}
		}

		return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
	}

	// For continuous spells and spells with a charge-up time. The count argument actually decrements by 1 each tick.
	// N.B. The first time this gets called is the tick AFTER use is called, not the same tick
	@Override
	public void onUsingTick(ItemStack stack, LivingEntity user, int count){

		if(user instanceof Player){

			Player player = (Player)user;

			Spell spell = WandHelper.getCurrentSpell(stack);

			SpellModifiers modifiers;

			if(WizardData.get(player) != null){
				modifiers = WizardData.get(player).itemCastingModifiers;
			}else{
				modifiers = this.calculateModifiers(stack, (Player)user, spell); // Fallback to the old way, should never be used
			}

			int useTick = stack.getUseDuration() - count;
			int chargeup = (int)(spell.getChargeup() * modifiers.get(SpellModifiers.CHARGEUP));

			if(spell.isContinuous){
				// Continuous spell charge-up is simple, just don't do anything until it's charged
				if(useTick >= chargeup){
					// castingTick needs to be relative to when the spell actually started
					int castingTick = useTick - chargeup;
					// Continuous spells (these must check if they can be cast each tick since the mana changes)
					// Don't call canCast when castingTick == 0 because we already did it in use - even
					// with charge-up times, because we don't want to trigger events twice
					if(castingTick == 0 || canCast(stack, spell, player, player.getUsedItemHand(), castingTick, modifiers)){
						cast(stack, spell, player, player.getUsedItemHand(), castingTick, modifiers);
					}else{
						// Stops the casting if it was interrupted, either by events or because the wand ran out of mana
						player.stopUsingItem();
					}
				}
			}else{
				// Non-continuous spells need to check they actually have a charge-up since ALL spells call setActiveHand
				if(chargeup > 0 && useTick == chargeup){
					// Once the spell is charged, it's exactly the same as in use
					cast(stack, spell, player, player.getUsedItemHand(), 0, modifiers);
				}
			}
		}
	}

	@Override
	public boolean canCast(ItemStack stack, Spell spell, Player caster, InteractionHand hand, int castingTick, SpellModifiers modifiers){

		// Spells can only be cast if the casting events aren't cancelled...
		if(castingTick == 0){
			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.WAND, spell, caster, modifiers))) return false;
		}else{
			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(Source.WAND, spell, caster, modifiers, castingTick))) return false;
		}

		int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding

		// As of wizardry 4.2 mana cost is only divided over two intervals each second
		if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

		// ...and the wand has enough mana to cast the spell...
		return cost <= this.getMana(stack) // This comes first because it changes over time
				// ...and the wand is the same tier as the spell or higher...
				&& spell.getTier().level <= this.tier.level
				// ...and either the spell is not in cooldown or the player is in creative mode
				&& (WandHelper.getCurrentCooldown(stack) == 0 || caster.isCreative());
	}

	@Override
	public boolean cast(ItemStack stack, Spell spell, Player caster, InteractionHand hand, int castingTick, SpellModifiers modifiers){

		Level world = caster.level;

		if(world.isClientSide && !spell.isContinuous && spell.requiresPacket()) return false;

		if(spell.cast(world, caster, hand, castingTick, modifiers)){

			if(castingTick == 0) MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.WAND, spell, caster, modifiers));

			if(!world.isClientSide){

				// Continuous spells never require packets so don't rely on the requiresPacket method to specify it
				if(!spell.isContinuous && spell.requiresPacket()){
					// Sends a packet to all players in dimension to tell them to spawn particles.
					PacketCastSpell.Message msg = new PacketCastSpell.Message(caster.getId(), hand, spell, modifiers);
					WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> world.dimension()), msg);
				}

				// Mana cost
				int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding
				// As of wizardry 4.2 mana cost is only divided over two intervals each second
				if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

				if(cost > 0) this.consumeMana(stack, cost, caster);

			}

			caster.startUsingItem(hand);

			// Cooldown
			if(!spell.isContinuous && !caster.isCreative()){ // Spells only have a cooldown in survival
				WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * modifiers.get(WizardryItems.COOLDOWN_UPGRADE.get())));
			}

			// Progression
			if(this.tier.level < Tier.MASTER.level && castingTick % CONTINUOUS_TRACKING_INTERVAL == 0){

				// We don't care about cost modifiers here, otherwise players would be penalised for wearing robes!
				int progression = (int)(spell.getCost() * modifiers.get(SpellModifiers.PROGRESSION));
				WandHelper.addProgression(stack, progression);

				if(!Wizardry.settings.legacyWandLevelling){ // Don't display the message if legacy wand levelling is enabled
					// If the wand just gained enough progression to be upgraded...
					Tier nextTier = tier.next();
					int excess = WandHelper.getProgression(stack) - nextTier.getProgression();
					if(excess >= 0 && excess < progression){
						// ...display a message above the player's hotbar
						caster.playSound(WizardrySounds.ITEM_WAND_LEVELUP, 1.25f, 1);
						WizardryAdvancementTriggers.wand_levelup.triggerFor(caster);
						if(!world.isClientSide)
							caster.sendSystemMessage(Component.translatable("item." + Wizardry.MODID + ":wand.levelup",
									this.getName(stack), nextTier.getNameForTranslationFormatted()));
					}
				}

				WizardData.get(caster).trackRecentSpell(spell);
			}

			return true;
		}

		return false;
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeLeft){

		if(user instanceof Player){

			Player player = (Player)user;

			Spell spell = WandHelper.getCurrentSpell(stack);

			SpellModifiers modifiers;

			if(WizardData.get(player) != null){
				modifiers = WizardData.get(player).itemCastingModifiers;
			}else{
				modifiers = this.calculateModifiers(stack, (Player)user, spell); // Fallback to the old way, should never be used
			}

			int castingTick = stack.getUseDuration() - timeLeft; // Might as well include this

			int cost = getDistributedCost((int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f), castingTick);

			// Still need to check there's enough mana or the spell will finish twice, since running out of mana is
			// handled separately.
			if(spell.isContinuous && spell.getTier().level <= this.tier.level && cost <= this.getMana(stack)){

				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Finish(Source.WAND, spell, player, modifiers, castingTick));
				spell.finishCasting(world, player, Double.NaN, Double.NaN, Double.NaN, null, castingTick, modifiers);

				if(!player.isCreative()){ // Spells only have a cooldown in survival
					WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * modifiers.get(WizardryItems.COOLDOWN_UPGRADE.get())));
				}
			}
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand){

		if(player.isShiftKeyDown() && entity instanceof Player && WizardData.get(player) != null){
			String string = WizardData.get(player).toggleAlly((Player)entity) ? "item." + Wizardry.MODID + ":wand.addally"
					: "item." + Wizardry.MODID + ":wand.removeally";
			if(!player.level.isClientSide) player.sendSystemMessage(Component.translatable(string, entity.getName()));
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	/** Distributes the given cost (which should be the per-second cost of a continuous spell) over a second and
	 * returns the appropriate cost to be applied for the given tick. Currently the cost is distributed over 2
	 * intervals per second, meaning the returned value is 0 unless {@code castingTick} is a multiple of 10.*/
	protected static int getDistributedCost(int cost, int castingTick){

		int partialCost;

		if(castingTick % 20 == 0){ // Whole number of seconds has elapsed
			partialCost = cost / 2 + cost % 2; // Make sure cost adds up to the correct value by adding the remainder here
		}else if(castingTick % 10 == 0){ // Something-and-a-half seconds has elapsed
			partialCost = cost/2;
		}else{ // Some other number of ticks has elapsed
			partialCost = 0; // Wands aren't damaged within half-seconds
		}

		return partialCost;
	}

	/** Returns a SpellModifiers object with the appropriate modifiers applied for the given ItemStack and Spell. */
	// This is now public because artefacts use it
	public SpellModifiers calculateModifiers(ItemStack stack, Player player, Spell spell){

		SpellModifiers modifiers = new SpellModifiers();

		// Now we only need to add multipliers if they are not 1.
		int level = WandHelper.getUpgradeLevel(stack, WizardryItems.RANGE_UPGRADE.get());
		if(level > 0)
			modifiers.set(WizardryItems.RANGE_UPGRADE.get(), 1.0f + level * Constants.RANGE_INCREASE_PER_LEVEL, true);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.DURATION_UPGRADE.get());
		if(level > 0)
			modifiers.set(WizardryItems.DURATION_UPGRADE.get(), 1.0f + level * Constants.DURATION_INCREASE_PER_LEVEL, false);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.BLAST_UPGRADE.get());
		if(level > 0)
			modifiers.set(WizardryItems.BLAST_UPGRADE.get(), 1.0f + level * Constants.BLAST_RADIUS_INCREASE_PER_LEVEL, true);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.COOLDOWN_UPGRADE.get());
		if(level > 0)
			modifiers.set(WizardryItems.COOLDOWN_UPGRADE.get(), 1.0f - level * Constants.COOLDOWN_REDUCTION_PER_LEVEL, true);

		float progressionModifier = 1.0f - ((float)WizardData.get(player).countRecentCasts(spell) / WizardData.MAX_RECENT_SPELLS)
				* MAX_PROGRESSION_REDUCTION;

		if(this.element == spell.getElement()){
			modifiers.set(SpellModifiers.POTENCY, 1.0f + (this.tier.level + 1) * Constants.POTENCY_INCREASE_PER_TIER, true);
			progressionModifier *= ELEMENTAL_PROGRESSION_MODIFIER;
		}

		if(WizardData.get(player) != null){

			if(!WizardData.get(player).hasSpellBeenDiscovered(spell)){
				// Casting an undiscovered spell now grants 5x progression
				progressionModifier *= DISCOVERY_PROGRESSION_MODIFIER;
			}

			if(!WizardData.get(player).hasReachedTier(this.tier.next())){
				// 1.5x progression for tiers that have already been reached
				progressionModifier *= SECOND_TIME_PROGRESSION_MODIFIER;
			}
		}

		modifiers.set(SpellModifiers.PROGRESSION, progressionModifier, false);

		return modifiers;
	}

	private boolean selectMinionTarget(Player player, Level world){

		HitResult rayTrace = RayTracer.standardEntityRayTrace(world, player, 16, false);

		if(rayTrace != null && EntityUtils.isLiving(((EntityHitResult) rayTrace).getEntity())){

			LivingEntity entity = (LivingEntity)((EntityHitResult) rayTrace).getEntity();

			// Sets the selected minion's target to the right-clicked entity
			if(player.isShiftKeyDown() && WizardData.get(player) != null && WizardData.get(player).selectedMinion != null){

				ISummonedCreature minion = WizardData.get(player).selectedMinion.get();

				if(minion instanceof Mob && minion != entity){
					// There is now only the new AI! (which greatly improves things)
					((Mob)minion).setTarget(entity);
					// Deselects the selected minion
					WizardData.get(player).selectedMinion = null;
					return true;
				}
			}
		}

		return false;
	}

	// Workbench stuff

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return BASE_SPELL_SLOTS + WandHelper.getUpgradeLevel(stack, WizardryItems.ATTUNEMENT_UPGRADE.get());
	}

	@Override
	public ItemStack applyUpgrade(@Nullable Player player, ItemStack wand, ItemStack upgrade){

		// Upgrades wand if necessary. Damage is copied, preserving remaining durability,
		// and also the entire NBT tag compound.
		if(upgrade.getItem() == WizardryItems.ARCANE_TOME.get()){

			Tier tier = Tier.values()[((IMetadata) upgrade.getItem()).getMetadata(upgrade)];

			// Checks the wand upgrade is for the tier above the wand's tier, and that either the wand has enough
			// progression or the player is in creative mode.
			if((player == null || player.isCreative() || Wizardry.settings.legacyWandLevelling
					|| WandHelper.getProgression(wand) >= tier.getProgression())
					&& tier == this.tier.next() && this.tier != Tier.MASTER){

				if(Wizardry.settings.legacyWandLevelling){
					// Progression has little meaning with legacy upgrade mechanics so just reset it
					// In theory, you can get 'free' progression when upgrading since progression can't be negative,
					// so the flipside of that is you lose any excess
					WandHelper.setProgression(wand, 0);
				}else{
					// Carry excess progression over to the new stack
					WandHelper.setProgression(wand, WandHelper.getProgression(wand) - tier.getProgression());
				}

				if(player != null) WizardData.get(player).setTierReached(tier);

				ItemStack newWand = new ItemStack(getWand(tier, this.element));
				newWand.setTag(wand.getTag());
				// This needs to be done after copying the tag compound so the mana capacity for the new wand
				// takes storage upgrades into account
				// Note the usage of the new wand item and not 'this' to ensure the correct capacity is used
				((IManaStoringItem)newWand.getItem()).setMana(newWand, this.getMana(wand));

				upgrade.shrink(1);

				return newWand;
			}

		}else if(WandHelper.isWandUpgrade(upgrade.getItem())){

			// Special upgrades
			Item specialUpgrade = upgrade.getItem();

			int maxUpgrades = this.tier.upgradeLimit;
			if(this.element == Element.MAGIC) maxUpgrades += Constants.NON_ELEMENTAL_UPGRADE_BONUS;

			if(WandHelper.getTotalUpgrades(wand) < maxUpgrades
					&& WandHelper.getUpgradeLevel(wand, specialUpgrade) < Constants.UPGRADE_STACK_LIMIT){

				// Used to preserve existing mana when upgrading storage rather than creating free mana.
				int prevMana = this.getMana(wand);

				WandHelper.applyUpgrade(wand, specialUpgrade);

				// Special behaviours for specific upgrades
				if(specialUpgrade == WizardryItems.STORAGE_UPGRADE.get()){

					this.setMana(wand, prevMana);

				}else if(specialUpgrade == WizardryItems.ATTUNEMENT_UPGRADE.get()){

					int newSlotCount = BASE_SPELL_SLOTS + WandHelper.getUpgradeLevel(wand,
							WizardryItems.ATTUNEMENT_UPGRADE.get());

					Spell[] spells = WandHelper.getSpells(wand);
					Spell[] newSpells = new Spell[newSlotCount];

					for(int i = 0; i < newSpells.length; i++){
						newSpells[i] = i < spells.length && spells[i] != null ? spells[i] : Spells.NONE;
					}

					WandHelper.setSpells(wand, newSpells);

					int[] cooldowns = WandHelper.getCooldowns(wand);
					int[] newCooldowns = new int[newSlotCount];

					if(cooldowns.length > 0){
						System.arraycopy(cooldowns, 0, newCooldowns, 0, cooldowns.length);
					}

					WandHelper.setCooldowns(wand, newCooldowns);
				}

				upgrade.shrink(1);

				if(player != null){

					WizardryAdvancementTriggers.special_upgrade.triggerFor(player);

					if(WandHelper.getTotalUpgrades(wand) == Tier.MASTER.upgradeLimit){
						WizardryAdvancementTriggers.max_out_wand.triggerFor(player);
					}
				}

			}
		}

		return wand;
	}

	/**
	 * Helper method to return the appropriate wand based on tier and element. This replaces the cumbersome wand map in
	 * {@link WizardryItems} by accessing the item registry dynamically by generating the registry name on the fly.
	 * <p></p>
	 * <i><b>This method will only return wands from the base mod.</b> It is unlikely that addons will need it, but it
	 * has been left public just in case. The intention is that this method is only used where there is no alternative.</i>
	 *
	 * @param tier The tier of the wand required.
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @return The wand item which corresponds to the given tier and element, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 */
	// As noted above, in a few SPECIFIC cases this method is necessary (without using a data-driven system, at least,
	// which I'm not going to spend the time making in the near future). Wizard trades and gear have been left using the
	// WizardryItems version because they need to be replaced with a better system that doesn't use this at all.
	public static Item getWand(Tier tier, Element element){
		if(tier == null) throw new NullPointerException("The given tier cannot be null.");
		if(element == null) element = Element.MAGIC;
		String registryName = tier == Tier.NOVICE && element == Element.MAGIC ? "magic" : tier.getUnlocalisedName();
		if(element != Element.MAGIC) registryName = registryName + "_" + element.getName();
		registryName = registryName + "_wand";
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID,  registryName));
	}

	@Override
	public boolean onApplyButtonPressed(Player player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		boolean changed = false; // Used for advancements

		if(upgrade.hasItem()){
			ItemStack original = centre.getItem().copy();
			centre.set(this.applyUpgrade(player, centre.getItem(), upgrade.getItem()));
			changed = !ItemStack.isSame(centre.getItem(), original);
		}

		// Reads NBT spell metadata array to variable, edits this, then writes it back to NBT.
		// Original spells are preserved; if a slot is left empty the existing spell binding will remain.
		// Accounts for spells which cannot be applied because they are above the wand's tier; these spells
		// will not bind but the existing spell in that slot will remain and other applicable spells will
		// be bound as normal, along with any upgrades and crystals.
		Spell[] spells = WandHelper.getSpells(centre.getItem());
		
		if(spells.length <= 0){
			// Base value here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
			spells = new Spell[BASE_SPELL_SLOTS];
		}
		
		for(int i = 0; i < spells.length; i++){
			if(spellBooks[i].getItem() != ItemStack.EMPTY){
				
				Spell spell = Spell.byMetadata(((IMetadata) spellBooks[i].getItem().getItem()).getMetadata(spellBooks[i].getItem()));
				// If the wand is powerful enough for the spell, it's not already bound to that slot and it's enabled for wands
				if(!(spell.getTier().level > this.tier.level) && spells[i] != spell && spell.isEnabled(SpellProperties.Context.WANDS)){

					// Decide if we can bind this multiple times
					if (Wizardry.settings.preventBindingSameSpellTwiceToWands && Arrays.stream(spells).anyMatch(s -> s == spell)) {
						continue;
					}

					spells[i] = spell;
					changed = true;

					// setting to consume books upon use
					if (Wizardry.settings.singleUseSpellBooks) {
						spellBooks[i].getItem().shrink(1);
					}
				}
			}
		}
		
		WandHelper.setSpells(centre.getItem(), spells);

		// Charges wand by appropriate amount
		if (WandHelper.rechargeManaOnApplyButtonPressed(centre, crystals)) {
			changed = true;
		}

		return changed;
	}

	@Override
	public void onClearButtonPressed(Player player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		ItemStack stack = centre.getItem();
		if (stack.hasTag() && stack.getTag().contains(WandHelper.SPELL_ARRAY_KEY)) {
			CompoundTag nbt = stack.getTag();
			int[] spells = nbt.getIntArray(WandHelper.SPELL_ARRAY_KEY);
			int expectedSlotCount = BASE_SPELL_SLOTS + WandHelper.getUpgradeLevel(stack,
					WizardryItems.ATTUNEMENT_UPGRADE.get());

			// unbrick broken wands
			if (spells.length < expectedSlotCount) {
				spells = new int[expectedSlotCount];
			}
			Arrays.fill(spells, 0);
			nbt.putIntArray(WandHelper.SPELL_ARRAY_KEY, spells);
			stack.setTag(nbt);
		}
	}

	@Override
	public boolean isClearable() { return true; }

	// hitEntity is only called server-side, so we'll have to use events
	@SubscribeEvent
	public static void onAttackEntityEvent(AttackEntityEvent event){

		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem(); // Can't melee with offhand items

		if(stack.getItem() instanceof IManaStoringItem){

			// Nobody said it had to be a wand, as long as it's got a melee upgrade it counts
			int level = WandHelper.getUpgradeLevel(stack, WizardryItems.MELEE_UPGRADE.get());
			int mana = ((IManaStoringItem)stack.getItem()).getMana(stack);

			if(level > 0 && mana > 0){

				RandomSource random = player.level.random;

				player.level.playLocalSound(player.getX(), player.getY(), player.getZ(), WizardrySounds.ITEM_WAND_MELEE, SoundSource.PLAYERS, 0.75f, 1, false);

				if(player.level.isClientSide){

					Vec3 origin = player.getEyePosition(1);
					Vec3 hit = origin.add(player.getLookAngle().scale(player.distanceTo(event.getTarget())));
					// Generate two perpendicular vectors in the plane perpendicular to the look vec
					Vec3 vec1 = player.getLookAngle().xRot(90);
					Vec3 vec2 = player.getLookAngle().cross(vec1);

					for(int i = 0; i < 15; i++){
						ParticleBuilder.create(Type.SPARKLE).pos(hit)
								.vel(vec1.scale(random.nextFloat() * 0.3f - 0.15f).add(vec2.scale(random.nextFloat() * 0.3f - 0.15f)))
								.clr(1f, 1f, 1f).fade(0.3f, 0.5f, 1)
								.time(8 + random.nextInt(4)).spawn(player.level);
					}
				}
			}
		}
	}

}
