package electroblob.wizardry.util;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import net.minecraft.world.inventory.Slot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * <i>"Never fear, {@code WandHelper} is here!"</i>
 * <p></p>
 * Much like {@link EnchantmentHelper EnchantmentHelper}, this class has some static methods
 * which allow cleaner and more concise interaction with the wand NBT data, which is quite a complex structure. Such
 * interaction previously resulted in rather verbose and repetitive code which was hard to read and even harder to
 * debug! For example, this class allowed {@link electroblob.wizardry.item.ItemWand ItemWand} to be shortened by about
 * 80 lines. In addition, by having all the various null checks and array size checks in one place, the chance of
 * accidental errors due to forgetting to check these things is greatly reduced.
 * <p></p>
 * Note that these methods contain no game logic at all; they are purely for interacting with the NBT data. Conversely,
 * you should never need to access the wand's NBT data directly when using this class, but the keys are public in the
 * unlikely case that this is necessary.
 * <p></p>
 * Also note that none of the methods in this class actually check that the given ItemStack contains an ItemWand; you
 * can, for example, pass in a stack of snowballs without causing problems, but that is of course pointless! However, if
 * you have your own spell casting item (which doesn't extend ItemWand), this setup means you can still use this class
 * to manage its NBT structure.
 * <p></p>
 * All <b>get</b> methods in this class return some kind of default if the passed-in wand stack has no nbt data. See
 * individual method descriptions for more details.<br>
 * All <b>set</b> methods in this class create a new nbt data for the passed-in wand if it has none, before doing
 * whatever else they do.
 * 
 * @see electroblob.wizardry.item.ItemWand ItemWand
 * @see electroblob.wizardry.packet.PacketControlInput PacketControlInput
 * @since Wizardry 1.1
 */
public final class WandHelper {

	// NBT tag keys
	public static final String SPELL_ARRAY_KEY = "spells";
	public static final String SELECTED_SPELL_KEY = "selectedSpell";
	public static final String COOLDOWN_ARRAY_KEY = "cooldown";
	public static final String MAX_COOLDOWN_ARRAY_KEY = "maxCooldown";
	public static final String UPGRADES_KEY = "upgrades";
	public static final String PROGRESSION_KEY = "progression";

	private static final HashMap<Item, String> upgradeMap = new HashMap<>();

	// =================================================== Spells ===================================================

	/**
	 * Returns an array containing the spells currently bound to the given wand. As of Wizardry 1.1, this array is not
	 * always the same size; it can be anywhere between 5 and 8 (inclusive) in length. If the wand has no spell data,
	 * returns an array of length 0.
	 */
	public static Spell[] getSpells(ItemStack wand){

		Spell[] spells = new Spell[0];

		if(wand.getTag() != null){

			int[] spellIDs = wand.getTag().getIntArray(SPELL_ARRAY_KEY);

			spells = new Spell[spellIDs.length];

			for(int i = 0; i < spellIDs.length; i++){
				spells[i] = Spell.byMetadata(spellIDs[i]);
			}
		}

		return spells;
	}

	/**
	 * Binds the given array of spells to the given wand. The array can be anywhere between 5 and 8 (inclusive) in
	 * length.
	 */
	public static void setSpells(ItemStack wand, Spell[] spells){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		int[] spellIDs = new int[spells.length];

		for(int i = 0; i < spells.length; i++){
			spellIDs[i] = spells[i] != null ? spells[i].metadata() : Spells.none.metadata();
		}

		wand.getTag().putIntArray(SPELL_ARRAY_KEY, spellIDs);
	}

	/** Returns the currently selected spell for the given wand, or the 'none' spell if the wand has no spell data. */
	public static Spell getCurrentSpell(ItemStack wand){

		Spell[] spells = getSpells(wand);

		if(wand.getTag() != null){

			int selectedSpell = wand.getTag().getInt(SELECTED_SPELL_KEY);

			if(selectedSpell >= 0 && selectedSpell < spells.length){
				return spells[selectedSpell];
			}
		}

		return Spells.none;
	}
	
	/** Returns the spell after the currently selected spell for the given wand, or the 'none' spell if the wand has no
	 * spell data. */
	public static Spell getNextSpell(ItemStack wand){

		Spell[] spells = getSpells(wand);
		int index = getNextSpellIndex(wand);

		if(index >= 0 && index < spells.length){
			return spells[index];
		}

		return Spells.none;
	}
	
	/** Returns the spell before the currently selected spell for the given wand, or the 'none' spell if the wand has no
	 * spell data. */
	public static Spell getPreviousSpell(ItemStack wand){

		Spell[] spells = getSpells(wand);
		int index = getPreviousSpellIndex(wand);

		if(index >= 0 && index < spells.length){
			return spells[index];
		}

		return Spells.none;
	}

	/** Selects the next spell in this wand's list of spells. */
	public static void selectNextSpell(ItemStack wand){
		// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
		if(getSpells(wand).length < 0) setSpells(wand, new Spell[ItemWand.BASE_SPELL_SLOTS]);

		if(wand.getTag() != null){
			wand.getTag().putInteger(SELECTED_SPELL_KEY, getNextSpellIndex(wand));
		}
	}

	/** Selects the previous spell in this wand's list of spells. */
	public static void selectPreviousSpell(ItemStack wand){
		// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
		if(getSpells(wand).length < 0) setSpells(wand, new Spell[ItemWand.BASE_SPELL_SLOTS]);

		if(wand.getTag() != null){
			wand.getTag().putInteger(SELECTED_SPELL_KEY, getPreviousSpellIndex(wand));
		}
	}

	/**
	 * Selects the spell at the given index in this wand's list of spells.
	 * @param wand The stack to set the spell of
	 * @param index The spell index to set the wand to
	 * @return False if the index was out-of-bounds, true otherwise
	 */
	public static boolean selectSpell(ItemStack wand, int index){

		if(index < 0 || index > getSpells(wand).length) return false; // Out-of-bounds

		// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
		if(getSpells(wand).length < 0) setSpells(wand, new Spell[ItemWand.BASE_SPELL_SLOTS]);

		if(wand.getTag() != null){
			wand.getTag().putInteger(SELECTED_SPELL_KEY, index);
		}

		return true;
	}
	
	private static int getNextSpellIndex(ItemStack wand){

		if(wand.getTag() == null) wand.setTag(new CompoundTag());
		
		int numberOfSpells = getSpells(wand).length;
		int spellIndex = wand.getTag().getInt(SELECTED_SPELL_KEY);
		
		// Greater than or equal to so that if attunement upgrades are somehow removed by NBT modification it just
		// resets.
		if(spellIndex >= numberOfSpells - 1){
			spellIndex = 0;
		}else{
			spellIndex++;
		}
		
		return spellIndex;
	}
	
	private static int getPreviousSpellIndex(ItemStack wand){

		if(wand.getTag() == null) wand.setTag(new CompoundTag());
		
		int numberOfSpells = getSpells(wand).length;
		int spellIndex = wand.getTag().getInt(SELECTED_SPELL_KEY);

		if(spellIndex <= 0){
			spellIndex = Math.max(0, numberOfSpells - 1);
		}else{
			spellIndex--;
		}

		return spellIndex;
	}

	// ================================================== Cooldowns ==================================================

	/**
	 * Returns an array of the cooldowns for each spell bound to the given wand. As of Wizardry 1.1, this array is not
	 * always the same size; it can be anywhere between 5 and 8 (inclusive) in length. If the wand has no cooldown data,
	 * returns an array of length 0.
	 */
	public static int[] getCooldowns(ItemStack wand){

		int[] cooldowns = new int[0];

		if(wand.getTag() != null){

			return wand.getTag().getIntArray(COOLDOWN_ARRAY_KEY);
		}

		return cooldowns;
	}

	/** Sets the given wand's cooldown array. The array can be anywhere between 5 and 8 (inclusive) in length.
	 * Unlike {@link WandHelper#setCurrentCooldown(ItemStack, int)}, this will <b>not</b> set the max cooldowns. */
	public static void setCooldowns(ItemStack wand, int[] cooldowns){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		wand.getTag().putIntArray(COOLDOWN_ARRAY_KEY, cooldowns);
	}

	/** Decrements the cooldown for each spell bound to the given wand by 1, if that cooldown is greater than 0. */
	public static void decrementCooldowns(ItemStack wand){

		int[] cooldowns = getCooldowns(wand);

		// If there are no cooldowns, it is assumed that they are all zero and therefore nothing needs to be done.
		if(cooldowns.length == 0) return;

		for(int i = 0; i < cooldowns.length; i++){
			if(cooldowns[i] > 0) cooldowns[i]--;
			if(cooldowns[i] < 0) cooldowns[i] = 0; // In case it got broken
		}

		setCooldowns(wand, cooldowns);
	}

	/** Returns the given wand's cooldown for the currently selected spell, or 0 if the wand has no cooldown data. */
	public static int getCurrentCooldown(ItemStack wand){

		if(wand.getTag() == null) wand.setTag(new CompoundTag());

		int[] cooldowns = getCooldowns(wand);

		int selectedSpell = wand.getTag().getInt(SELECTED_SPELL_KEY);

		if(selectedSpell < 0 || cooldowns.length <= selectedSpell) return 0;
		// Don't need to check if the tag compound is null since the above check is equivalent.
		return cooldowns[selectedSpell];
	}
	
	/** Returns the given wand's cooldown for the spell after the currently selected spell, or 0 if the wand has no
	 * cooldown data. */
	public static int getNextCooldown(ItemStack wand){

		int[] cooldowns = getCooldowns(wand);

		int nextSpell = getNextSpellIndex(wand);

		if(nextSpell < 0 || cooldowns.length <= nextSpell) return 0;
		// Don't need to check if the tag compound is null since the above check is equivalent.
		return cooldowns[nextSpell];
	}
	
	/** Returns the given wand's cooldown for the spell before the currently selected spell, or 0 if the wand has no
	 * cooldown data. */
	public static int getPreviousCooldown(ItemStack wand){

		int[] cooldowns = getCooldowns(wand);

		int previousSpell = getPreviousSpellIndex(wand);

		if(previousSpell < 0 || cooldowns.length <= previousSpell) return 0;
		// Don't need to check if the tag compound is null since the above check is equivalent.
		return cooldowns[previousSpell];
	}

	/** Sets the given wand's cooldown for the currently selected spell. Will also set the maximum cooldown. */
	public static void setCurrentCooldown(ItemStack wand, int cooldown){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		int[] cooldowns = getCooldowns(wand);

		int selectedSpell = wand.getTag().getInt(SELECTED_SPELL_KEY);
		int spellCount = getSpells(wand).length;

		if(spellCount <= selectedSpell) return; // Probably shouldn't happen

		// The length of the spells array must be greater than 0 since this method can only be called if a spell is
		// cast, which is impossible if there are no spells.
		if(cooldowns.length <= selectedSpell) cooldowns = new int[spellCount];

		if(cooldown <= 0) cooldown = 1;

		cooldowns[selectedSpell] = cooldown;

		setCooldowns(wand, cooldowns);

		int[] maxCooldowns = getMaxCooldowns(wand);

		if(maxCooldowns.length <= selectedSpell) maxCooldowns = new int[spellCount];

		maxCooldowns[selectedSpell] = cooldown;

		setMaxCooldowns(wand, maxCooldowns);
	}

	/**
	 * Returns an array of the max cooldowns for each spell bound to the given wand. If the wand has no cooldown data,
	 * returns an array of length 0.
	 */
	public static int[] getMaxCooldowns(ItemStack wand){

		int[] cooldowns = new int[0];

		if(wand.getTag() != null){

			return wand.getTag().getIntArray(MAX_COOLDOWN_ARRAY_KEY);
		}

		return cooldowns;
	}

	/** Sets the given wand's cooldown array. The array can be anywhere between 5 and 8 (inclusive) in length. */
	public static void setMaxCooldowns(ItemStack wand, int[] cooldowns){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		wand.getTag().putIntArray(MAX_COOLDOWN_ARRAY_KEY, cooldowns);
	}

	/** Returns the given wand's max cooldown for the currently selected spell, or 0 if the wand has no cooldown data. */
	public static int getCurrentMaxCooldown(ItemStack wand){

		int[] cooldowns = getMaxCooldowns(wand);

		if(wand.getTag() == null) return 0;

		int selectedSpell = wand.getTag().getInt(SELECTED_SPELL_KEY);

		if(selectedSpell < 0 || cooldowns.length <= selectedSpell) return 0;

		return cooldowns[selectedSpell];
	}

	// ================================================== Upgrades ==================================================

	/**
	 * Returns the number of upgrades of the given type that have been applied to the given wand, or 0 if the wand has
	 * no upgrade data or the given item is not a valid wand upgrade.
	 */
	public static int getUpgradeLevel(ItemStack wand, Item upgrade){

		String key = upgradeMap.get(upgrade);

		if(wand.getTag() != null && wand.getTag().contains(UPGRADES_KEY) && key != null){
			return wand.getTag().getCompound(UPGRADES_KEY).getInt(key);
		}

		return 0;

	}

	/**
	 * Returns the total number of upgrades that have been applied to the given wand, or 0 if the wand has no upgrade
	 * data.
	 */
	public static int getTotalUpgrades(ItemStack wand){

		int totalUpgrades = 0;

		for(Item item : upgradeMap.keySet()){
			totalUpgrades += getUpgradeLevel(wand, item);
		}

		return totalUpgrades;
	}

	/**
	 * Applies the given upgrade to the given wand, or in other words increases the level for that upgrade by 1. This
	 * does <b>not</b> account for the individual or total upgrade stack limits or any special behaviour; it only deals
	 * with the NBT data.
	 */
	public static void applyUpgrade(ItemStack wand, Item upgrade){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		if(!wand.getTag().contains(UPGRADES_KEY))
			NBTExtras.storeTagSafely(wand.getTag(), UPGRADES_KEY, new CompoundTag());

		CompoundTag upgrades = wand.getTag().getCompound(UPGRADES_KEY);

		String key = upgradeMap.get(upgrade);

		if(key != null) upgrades.putInt(key, upgrades.getInt(key) + 1);

		NBTExtras.storeTagSafely(wand.getTag(), UPGRADES_KEY, upgrades);
	}

	/** Returns true if the given item is a valid special wand upgrade. */
	public static boolean isWandUpgrade(Item upgrade){
		return upgradeMap.containsKey(upgrade);
	}

	/** Returns an unmodifiable set of all the items which are valid special wand upgrades. */
	public static Set<Item> getSpecialUpgrades(){
		return Collections.unmodifiableSet(WandHelper.upgradeMap.keySet());
	}

	/**
	 * Package-protected getter for the identifier that corresponds to the given item, used only in the
	 * {@link SpellModifiers} class. Internal to Wizardry.
	 * 
	 * @throws IllegalArgumentException if the given item is not a registered special wand upgrade.
	 */
	static String getIdentifier(Item upgrade){
		if(!isWandUpgrade(upgrade)) throw new IllegalArgumentException(
				"Tried to get a wand upgrade key for an item" + "that is not a registered special wand upgrade.");
		return upgradeMap.get(upgrade);
	}

	/**
	 * Registers a special upgrade with wizardry. Not used in the base mod, but I've put it here to make it easy for
	 * add-ons to add new wand upgrades. This should be called during the init() phase of mod loading.
	 * 
	 * @param upgrade The wand upgrade item
	 * @param identifier A unique string, used as a key for wand nbt tags
	 * @throws IllegalArgumentException if the passed in identifier is already used for another wand upgrade
	 */
	public static void registerSpecialUpgrade(Item upgrade, String identifier){
		// Throwing an exception is the best thing to do here, since if a duplicate was allowed weird things would
		// happen later with wand NBT.
		if(upgradeMap.containsValue(identifier))
			throw new IllegalArgumentException("Duplicate wand upgrade identifier: " + identifier);
		upgradeMap.put(upgrade, identifier);
	}

	/** Called from the init() method in wizardry's main mod class to populate the special wand upgrade map. */
	public static void populateUpgradeMap(){
		upgradeMap.put(WizardryItems.condenser_upgrade, "condenser");
		upgradeMap.put(WizardryItems.storage_upgrade, "storage");
		upgradeMap.put(WizardryItems.siphon_upgrade, "siphon");
		upgradeMap.put(WizardryItems.range_upgrade, "range");
		upgradeMap.put(WizardryItems.duration_upgrade, "duration");
		upgradeMap.put(WizardryItems.cooldown_upgrade, "cooldown");
		upgradeMap.put(WizardryItems.blast_upgrade, "blast");
		upgradeMap.put(WizardryItems.attunement_upgrade, "attunement");
		upgradeMap.put(WizardryItems.melee_upgrade, "melee");
	}

	// ================================================= Progression =================================================

	/** Sets the given wand's progression to the given value. */
	public static void setProgression(ItemStack wand, int progression){

		if(wand.getTag() == null) wand.setTag((new CompoundTag()));

		wand.getTag().putInteger(PROGRESSION_KEY, progression);
	}

	/** Returns the progression value for the given wand, or 0 if the wand has no data. */
	public static int getProgression(ItemStack wand){

		if(wand.getTag() == null) return 0;

		return wand.getTag().getInt(PROGRESSION_KEY);
	}

	/** Adds the given amount of progression to this wand's progression value. */
	public static void addProgression(ItemStack wand, int progression){
		setProgression(wand, getProgression(wand) + progression);
	}

	/**
	 * Recharges the mana of an item when the apply button is pressed in the arcane workbench GUI.
	 * This method requires the central item to implement both the {@link IWorkbenchItem} and {@link IManaStoringItem} interfaces.
	 *
	 * @param centre The slot representing the central item to be recharged.
	 * @param crystals The slot containing mana crystals for recharging.
	 * @return {@code true} if the mana of the central item was successfully recharged, {@code false} otherwise.
	 */
	public static boolean rechargeManaOnApplyButtonPressed(Slot centre, Slot crystals) {
		boolean changed = false;
		if (!(centre.getItem().getItem() instanceof IWorkbenchItem) || !(centre.getItem().getItem() instanceof IManaStoringItem)) {
			return false;
		}
		IManaStoringItem iManaStoringItem = (IManaStoringItem) centre.getItem().getItem();

		// Charges the item by appropriate amount
		if (crystals.getItem() != ItemStack.EMPTY && !iManaStoringItem.isManaFull(centre.getItem())) {

			int chargeDepleted = iManaStoringItem.getManaCapacity(centre.getItem()) - iManaStoringItem.getMana(centre.getItem());

			// Not too pretty but allows addons implementing the IManaStoringItem interface to provide their mana amount for custom crystals,
			// previously this was defaulted to the regular crystal's amount, allowing players to exploit it if a crystal was worth less mana than that.
			int manaPerItem = crystals.getItem().getItem() instanceof IManaStoringItem ?
					((IManaStoringItem) crystals.getItem().getItem()).getMana(crystals.getItem()) :
					crystals.getItem().getItem() instanceof ItemCrystal ? Constants.MANA_PER_CRYSTAL : Constants.MANA_PER_SHARD;

			if (crystals.getItem().getItem() == WizardryItems.crystal_shard) {manaPerItem = Constants.MANA_PER_SHARD;}
			if (crystals.getItem().getItem() == WizardryItems.grand_crystal) {manaPerItem = Constants.GRAND_CRYSTAL_MANA;}

			if (crystals.getItem().getCount() * manaPerItem < chargeDepleted) {
				// If there aren't enough crystals to fully charge the item
				iManaStoringItem.rechargeMana(centre.getItem(), crystals.getItem().getCount() * manaPerItem);
				crystals.decrStackSize(crystals.getItem().getCount());

			} else {
				// If there are excess crystals (or just enough)
				iManaStoringItem.setMana(centre.getItem(), iManaStoringItem.getManaCapacity(centre.getItem()));
				crystals.decrStackSize((int) Math.ceil(((double) chargeDepleted) / manaPerItem));
			}

			changed = true;
		}

		return changed;
	}
}
