package electroblob.wizardry.data;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.EvictingQueue;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.enchantment.Imbuement;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketPlayerSync;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

/**
 * Capability-based replacement for the old ExtendedPlayer class from 1.7.10. This has been reworked to leave minimum
 * external changes (for my own sanity, mainly!). Turns out the only major difference between an internal capability and
 * an IEEP is a couple of redundant classes and a different way of registering it.
 * <p></p>
 * Forge seems to have separate classes to hold the Capability<...> instance ('key') and methods for getting the
 * capability, but in my opinion there are already too many classes to deal with, so I'm not adding any more than are
 * necessary, meaning those constants and values are kept here instead.
 * 
 * @since Wizardry 2.1
 * @author Electroblob
 */
// On the plus side, having to rethink this class allowed me to clean it up a lot.
@Mod.EventBusSubscriber
public class WizardData implements INBTSerializable<CompoundTag> {

	/** Static instance of what I like to refer to as the capability key. Private because, well, it's internal! */
    private static final Capability<WizardData> WIZARD_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    	
    });

	/** Internal storage of registered variable keys. This only contains the stored keys. */
	private static final Set<IStoredVariable> storedVariables = new HashSet<>();

	/** The maximum number of recent spells to track. */
	public static final int MAX_RECENT_SPELLS = ItemWand.BASE_SPELL_SLOTS;

	private static final int IMBUEMENT_UPDATE_INTERVAL = 20;

	/** The player this WizardData instance belongs to. */
	private final Player player;

	/** An instance of {@link Random} which is <i>guaranteed</i> to produce the same number sequence client and server
	 * side <i>provided that it is always called from common code.</i> This can be useful in reducing the number of
	 * packets sent in certain situations.<br>
	 * <br>
	 * This is achieved by setting the seed to a new random value each time {@link WizardData#sync()} is called and
	 * sending this to the client so it can also set its seed to that value. */
	public final Random synchronisedRandom;

	/** Whether this player is currently casting a continuous spell via commands. Not saved over world reload and reset
	 * on player death. */
	private Spell castCommandSpell;
	/** The time for which this player has been casting a continuous spell via commands. Increments by 1 each tick. Not
	 * saved over world reload and reset on player death. */
	private int castCommandTick;
	/** SpellModifiers object for the current continuous spell cast via commands. Not saved over world reload and reset
	 * on player death. */
	private SpellModifiers castCommandModifiers;
	/** The number of ticks this player's current continuous spell lasts for, or null if there is none. Not saved over
	 * world reload and reset on player death. */
	private int castCommandDuration;

	/** SpellModifiers object for the current continuous spell cast via items. Not saved over world reload and reset
	 * on player death. <i>N.B. Since a player can only use one item at a time, this can be reused for any item that
	 * casts spells, it's not just for wands.</i>*/
	public SpellModifiers itemCastingModifiers;

	public WeakReference<ISummonedCreature> selectedMinion;

	/** Set of this player's discovered spells. <b>Do not write to this list directly</b>, use
	 * {@link WizardData#discoverSpell(Spell)} instead. */
	public Set<Spell> spellsDiscovered;

	/** The highest {@link Tier} this player has upgraded a wand to, used to apply progression modifiers. */
	private Tier maxTierReached = Tier.NOVICE;

	private Set<UUID> allies;
	/** List of usernames of this player's allies. May not be accurate 100% of the time. This is here so that a player
	 * can view the usernames of their allies even when those allies are not online. <b> Do not use this for any other
	 * purpose than displaying the names! */
	public Set<String> allyNames;

	/** Internal storage of custom (spell-specific) data. Note that a {@code Map} cannot specify that its values are of
	 * the same type as the type parameter of its keys, so to ensure this condition always holds, the map must only
	 * be modified via {@link WizardData#setVariable(IVariable, Object)}, which (as a method) is able to enforce it. */
	private final Map<IVariable, Object> spellData;

	private Queue<Spell> recentSpells;

	// This one is still necessary, because I can't override the equip animation for items that aren't from Wizardry.
	// Leaving this for now because merging it into the spell data system will be more tricky
	private Map<Imbuement, Integer> imbuementDurations;

	/** Stores this player's y velocity from the previous tick; used for the velocity-based fall damage replacement. */
	public double prevMotionY;

	public WizardData(){
		this(null); // Nullary constructor for the registration method factory parameter
	}

	public WizardData(Player player){
		this.player = player;
		this.synchronisedRandom = new Random();
		this.imbuementDurations = new HashMap<>();
		this.spellsDiscovered = new HashSet<>();
		// All players can recognise magic missile. This is not done using discoverSpell because that seems to cause
		// a crash on load occasionally (probably something to do with achievements being initialised)
		this.spellsDiscovered.add(Spells.MAGIC_MISSILE);
		this.recentSpells = EvictingQueue.create(MAX_RECENT_SPELLS); // Only keeps a reference to the last 5 spells cast
		this.castCommandSpell = Spells.NONE;
		this.castCommandModifiers = new SpellModifiers();
		this.castCommandTick = 0;
		this.itemCastingModifiers = new SpellModifiers();
		this.allies = new HashSet<>();
		this.allyNames = new HashSet<>();
		this.spellData = new HashMap<>();
	}

    public static void attachCapability(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player player) {
            e.addCapability(new ResourceLocation(Wizardry.MODID, "wizard_data"), new WizardData.Provider((Player) e.getObject()));
        }
    }

	/** Returns the WizardData instance for the specified player. */
	public static WizardData get(Player player){
		return player.getCapability(WIZARD_DATA_CAPABILITY).orElse(new WizardData(player));
	}

	// ============================================= Variable Storage =============================================

	// This is my answer to having spells define their own player variables. It's not the prettiest system ever, but
	// I think the ability to add arbitrary data to this class and have it save itself to NBT automatically is pretty
	// powerful. If it doesn't need saving, this can even be done on the fly - no registration necessary.

	// The reason we have interfaces here is to allow custom implementations of the NBT read/write methods, for
	// example, reading/writing multiple keys without having to wrap them in an NBTTagCompound.

	/** Registers the given {@link IStoredVariable} objects as keys that will be stored to NBT for each {@code WizardData}
	 * instance. */
	public static void registerStoredVariables(IStoredVariable<?>... variables){
		storedVariables.addAll(Arrays.asList(variables));
	}

	/** Returns a set containing the registered {@link IStoredVariable} objects for which {@link IVariable#isSynced()}
	 * returns true, ordered by their keys obtained from {@link IVariable#getKey()}. Used internally for packets. */
	public static Set<IVariable> getSyncedVariablesOrderedByKey(){
		Comparator<IVariable> keyComparator = Comparator.comparing(IVariable::getKey);

		return storedVariables.stream()
				.filter(IVariable::isSynced)
				.sorted(keyComparator)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Stores the given value under the given key in this {@code WizardData} object.
	 * @param variable The key under which the value is to be stored. See {@link IVariable} for more details.
	 * @param value The value to be stored.
	 * @param <T> The type of the value to be stored. Note that the given variable (key) may be of a supertype of the
	 *           stored value itself; however, when the value is retrieved its type will match that of the key. In
	 *           other words, if an {@code Integer} is stored under a {@code IVariable<Number>}, a {@code Number} will
	 *           be returned when the value is retrieved.
	 */
	// This use of type parameters guarantees that data may only be stored (and therefore may only be accessed)
	// using a compatible key. For instance, the following code will not compile:
	// Number i = 1;
	// setVariable(StoredVariable.ofInt("key", Persistence.ALWAYS), i);
	public <T> void setVariable(IVariable<? super T> variable, T value){
		this.spellData.put(variable, value);
	}

	/**
	 * Returns the value stored under the given key in this {@code WizardData} object, or null if the key was not
	 * stored.
	 * @param variable The key whose associated value is to be returned.
	 * @param <T> The type of the returned value.
	 * @return The value associated with the given key, or null no such key was stored. <i>Beware of auto-unboxing
	 * of primitive types! Directly assigning the result to a primitive type, as in {@code int i = getVariable(...)},
	 * will cause a {@link NullPointerException} if the key was not stored.</i>
	 */
	@SuppressWarnings("unchecked") // The spellData map is fully encapsulated so we can be sure that the cast is safe
	@Nullable
	public <T> T getVariable(IVariable<T> variable){
		return (T)spellData.get(variable);
	}

	// ============================================== Miscellaneous ==============================================

	// Spell discovery

	public boolean hasSpellBeenDiscovered(Spell spell){
		return spellsDiscovered.contains(spell) || spell instanceof None;
	}

	/**
	 * Adds the given spell to the list of discovered spells for this player. Automatically takes into account whether
	 * the spell has been discovered. Use this method rather than adding directly to the list because it handles
	 * achievements.
	 * 
	 * @param spell The spell to be discovered
	 * @return True if the spell had not already been discovered; false otherwise.
	 */
	public boolean discoverSpell(Spell spell){

		if(spellsDiscovered == null){
			spellsDiscovered = new HashSet<>();
		}
		// The 'none' spell cannot be discovered
		if(spell instanceof None) return false;
		// Tries to add the spell to the list of discovered spells, and returns false if it was already present
		return spellsDiscovered.add(spell);
	}

	/**
	 * Sets the tier this player has reached to the given tier, if they have not yet reached it.
	 * @param tier The tier the player has reached
	 */
	public void setTierReached(Tier tier){
		if(!hasReachedTier(tier)) this.maxTierReached = tier;
	}

	/**
	 * Returns true if this player has previously upgraded a wand to the given tier.
	 * @param tier The tier to check for
	 * @return True if this player has already upgraded a wand to the given tier, false if not. This does not include
	 * wands that were purchased at the given tier, unless they have since been upgraded.
	 */
	public boolean hasReachedTier(Tier tier){
		return tier.level >= maxTierReached.level;
	}

	// Recent spell tracking

	/**
	 * Adds the given spell to this player's recently-cast spells. Spells can (and will) be added multiple times, and
	 * will be automatically removed when enough spells are added after them.
	 * @param spell The spell to be tracked.
	 */
	public void trackRecentSpell(Spell spell){
		this.recentSpells.add(spell);
	}

	/**
	 * Returns the number of times the given spell is tracked in this player's recently-cast spells.
	 * @param spell The spell to count casts for.
	 */
	public int countRecentCasts(Spell spell){
		return (int)this.recentSpells.stream().filter(s -> s == spell).count(); // We know this can't be more than 5
	}

	// Imbuements

	/**
	 * Overwrites the imbuement duration associated with the given imubement for this player, or creates it if there was
	 * none previously.
	 * 
	 * @throws IllegalArgumentException if the given {@link Enchantment} is not an {@link Imbuement}.
	 */
	public void setImbuementDuration(Enchantment enchantment, int duration){
		// It is best to throw an exception here, because otherwise the error would either go unnoticed (if non-
		// -imbuements were ignored) or cause a ClassCastException later (if non-imbuements were allowed to be added)
		if(enchantment instanceof Imbuement){
			this.imbuementDurations.put((Imbuement)enchantment, duration);
		}else{
			throw new IllegalArgumentException(
					"Attempted to set an imbuement duration for something that isn't an Imbuement!");
		}
	}

	/**
	 * Returns the imbuement duration associated with the given imbuement for this player, or 0 if it does not exist.
	 */
	@SuppressWarnings("unlikely-arg-type")
	public int getImbuementDuration(Enchantment enchantment){
		// Need to check that i is not null, otherwise it throws an NPE when Java auto-unboxes it.
		// What's nice here is that the map simply accepts objects as keys, so there's no need to cast or throw
		// exceptions.
		Integer i = this.imbuementDurations.get(enchantment);
		// If i is null, returns 0; otherwise returns i, auto-unboxed to an int.
		return i == null ? 0 : i;
	}

	/**
	 * Decrements the duration for each conjured item by 1, and removes from the map any that are 0 or less or that the
	 * player no longer has. Also deletes the item from the player's inventory if it runs out of time.
	 */
	private void updateImbuedItems(){

		// FIXME: Iterating through the entire inventory each tick is an AWFUL way of doing this, and is super-slow!
		//        Change the imbuement durations map to be ItemStack -> Integer and just decrement them, adding entries
		//		  whenever an item with an imbuement enters the inventory and removing them when the time expires.

		Set<Imbuement> activeImbuements = new HashSet<Imbuement>();

		// For each item in the player's inventory
		for(ItemStack stack : player.getInventory().items){
			updateImbutedItem(stack, activeImbuements);
		}
		for(ItemStack stack : player.getInventory().armor){
			updateImbutedItem(stack, activeImbuements);
		}
		for(ItemStack stack : player.getInventory().offhand){
			updateImbutedItem(stack, activeImbuements);
		}

		// Removes all imbuements from the map that are no longer active
		this.imbuementDurations.keySet().retainAll(activeImbuements);
	}

	private void updateImbutedItem(ItemStack stack, Set<Imbuement> activeImbuements){

		if(stack.isEnchanted()){

			ListTag enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
					EnchantedBookItem.getEnchantments(stack) : stack.getEnchantmentTags();

			Iterator<Tag> iterator = enchantmentList.iterator();
			// For each of the item's enchantments
			while(iterator.hasNext()){
				CompoundTag enchantmentTag = (CompoundTag) iterator.next();
				Enchantment enchantment = Enchantment.byId(enchantmentTag.getShort("id"));
				// Ignores the enchantment unless it is an imbuement
				if(enchantment instanceof Imbuement){
					int duration = this.getImbuementDuration(enchantment);
					// If the imbuement is still active:
					if(duration > 0){
						// Decrements the timer
						this.imbuementDurations.put((Imbuement)enchantment, duration - IMBUEMENT_UPDATE_INTERVAL);
						// Adds this imbuement to the set of imbuements that need to be kept
						activeImbuements.add((Imbuement)enchantment);
					}else{
						// Otherwise, removes the enchantment from the item
						((Imbuement) enchantment).onImbuementRemoval(stack);
						iterator.remove(); // FIXME: Apparently this can cause a CME
					}
				}
			}
		}
	}

	// Ally designation system

	/**
	 * Adds the given player to the list of allies belonging to the associated player, or removes the player if they are
	 * already in the list of allies. Returns true if the player was added, false if they were removed.
	 */
	public boolean toggleAlly(Player player){
		if(this.isPlayerAlly(player)){
			this.allies.remove(player.getUUID());
			// The remove method uses .equals() rather than == so this will work fine.
			this.allyNames.remove(player.getName());
			return false;
		}else{
			this.allies.add(player.getUUID());
			this.allyNames.add(player.getName().getString());
			return true;
		}
	}

	/** Returns whether the given player is in this player's list of allies, or is on the same team as this player. */
	public boolean isPlayerAlly(Player player){
		return this.allies.contains(player.getUUID()) || this.player.isAlliedTo(player);
	}

	/** Returns whether the player with the given UUID is in this player's list of allies. The player to whom the given
	 * UUID belongs need not be logged in. This method is intended for use by owned entities so that their owner's
	 * allies don't accidentally damage them, even when the owner is offline. */
	public boolean isPlayerAlly(UUID playerUUID){
		// Scoreboard teams use usernames, but since we keep a cache of those...
		return this.allies.contains(playerUUID) || (this.player.getTeam() != null && this.player.getTeam().getPlayers() != null
		&& this.player.getTeam().getPlayers().stream().anyMatch(allyNames::contains));
	}

	// Command continuous spell casting

	/** Starts casting the given spell with the given modifiers. */
	public void startCastingContinuousSpell(Spell spell, SpellModifiers modifiers, int duration){

		this.castCommandSpell = spell;
		this.castCommandModifiers = modifiers;
		this.castCommandDuration = duration;

		if(!this.player.level.isClientSide){
            PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player, spell, modifiers, duration);
            WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> this.player.level.dimension()), message);
		}
	}

	/** Stops casting the current spell. */
	public void stopCastingContinuousSpell(){

		this.castCommandSpell = Spells.NONE;
		this.castCommandTick = 0;
		this.castCommandModifiers.reset();

		if(!this.player.level.isClientSide){
			PacketCastContinuousSpell.Message message = new PacketCastContinuousSpell.Message(this.player, Spells.NONE, this.castCommandModifiers, this.castCommandDuration);
            WizardryPacketHandler.net.send(PacketDistributor.DIMENSION.with(() -> this.player.level.dimension()), message);
		}
	}

	/** Casts the current continuous spell, fires relevant events and updates the castCommandTick field. */
	public void updateContinuousSpellCasting(){

		if(this.castCommandSpell != null && this.castCommandSpell.isContinuous){

			if(castCommandTick >= castCommandDuration){
				this.stopCastingContinuousSpell();
				return;
			}

			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(Source.COMMAND, castCommandSpell, player, castCommandModifiers, castCommandTick))){
				this.stopCastingContinuousSpell();
				return;
			}

			if(this.castCommandSpell.cast(player.level, player, InteractionHand.MAIN_HAND, castCommandTick, this.castCommandModifiers)
					&& this.castCommandTick == 0){
				// On the first tick casting a continuous spell via commands, SpellCastEvent.Post is fired.
				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.COMMAND, castCommandSpell, player, castCommandModifiers));
			}

			castCommandTick++;

		}else{
			// Why is this here? Surely castCommandTick will always be 0 if castCommandSpell is null?
			this.castCommandTick = 0;
		}
	}

	/** Returns whether this player is currently casting a continuous spell via commands. */
	public boolean isCasting(){
		return this.castCommandSpell != null && this.castCommandSpell != Spells.NONE;
	}

	/**
	 * Returns the continuous spell this player is currently casting via commands, or the 'none' spell if they aren't
	 * casting anything.
	 */
	public Spell currentlyCasting(){
		return castCommandSpell;
	}

	// ============================================== Data Handling ==============================================

	/** Called each time the associated player is updated. */
	@SuppressWarnings("unchecked") // Again, we know it must be ok
	private void update(){

		if(this.selectedMinion != null && this.selectedMinion.get() == null) this.selectedMinion = null;

		prevMotionY = player.getDeltaMovement().y;

		// This new system removes a lot of repetitive event handler code and inflexible data which had duplicate
		// functions, just for different enchantments.
		if(player.tickCount % IMBUEMENT_UPDATE_INTERVAL == 0) updateImbuedItems();
		updateContinuousSpellCasting();

		this.spellData.forEach((k, v) -> this.spellData.put(k, k.update(player, v)));
		this.spellData.keySet().removeIf(k -> k.canPurge(player, this.spellData.get(k)));
	}

	/**
	 * Called from the event handler each time the associated player entity is cloned, i.e. on respawn or when
	 * travelling to a different dimension. Used to copy over any data that should persist over player death. This
	 * is the inverse of the old onPlayerDeath method, which reset the data that shouldn't persist.
	 * 
	 * @param data The old WizardData whose data is to be copied over.
	 * @param respawn True if the player died and is respawning, false if they are just travelling between dimensions.
	 */
	public void copyFrom(WizardData data, boolean respawn){

		this.allies = data.allies;
		this.allyNames = data.allyNames;
		this.selectedMinion = data.selectedMinion;
		this.spellsDiscovered = data.spellsDiscovered;
		this.maxTierReached = data.maxTierReached;
		this.recentSpells = data.recentSpells;

		for(IVariable variable : data.spellData.keySet()){
			if(variable.isPersistent(respawn)) this.spellData.put(variable, data.spellData.get(variable));
		}

		// Imbuements are lost on death so their durations do not persist.
		// Command spell casting is reset on death so the associated variables do not persist.
	}

	/** Sends a packet to this player's client to synchronise necessary information. Only called server side. */
	public void sync(){
		if(this.player instanceof ServerPlayer){
			int id = -1;
			if(this.selectedMinion != null && this.selectedMinion.get() instanceof Entity)
				id = ((Entity)this.selectedMinion.get()).getId();
			long seed = player.level.random.nextLong();
			this.synchronisedRandom.setSeed(seed);
			PacketPlayerSync.Message msg = new PacketPlayerSync.Message(seed, this.spellsDiscovered, id, this.spellData);
			WizardryPacketHandler.net.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)this.player), msg);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public CompoundTag serializeNBT(){

		CompoundTag properties = new CompoundTag();

		NBTExtras.storeTagSafely(properties, "imbuements", NBTExtras.mapToNBT(this.imbuementDurations,
				imbuement -> IntTag.valueOf(((ForgeRegistry<Enchantment>) ForgeRegistries.ENCHANTMENTS).getID((Enchantment) imbuement)), IntTag::valueOf));

		// Mmmmmm Java 8....
		NBTExtras.storeTagSafely(properties, "allies", NBTExtras.listToNBT(this.allies, NbtUtils::createUUID));
		NBTExtras.storeTagSafely(properties, "allyNames", NBTExtras.listToNBT(this.allyNames, StringTag::valueOf));

		// Might be worth converting this over to WizardryUtilities.listToNBT.
		int[] spells = new int[this.spellsDiscovered.size()];
		int i = 0;
		for(Spell spell : this.spellsDiscovered){
			spells[i] = spell.metadata();
			i++;
		}
		properties.putIntArray("discoveredSpells", spells);

		properties.putInt("maxTierReached", maxTierReached.ordinal());

		NBTExtras.storeTagSafely(properties, "recentSpells", NBTExtras.listToNBT(recentSpells, s -> IntTag.valueOf(s.metadata())));

		storedVariables.forEach(k -> k.write(properties, this.spellData.get(k)));

		return properties;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt){

		if(nbt != null){

			this.imbuementDurations = NBTExtras.NBTToMap(nbt.getList("imbuements", Tag.TAG_COMPOUND),
					(IntTag tag) -> (Imbuement)Enchantment.byId(tag.getAsInt()), IntTag::getAsInt);

			this.allies = new HashSet<>(NBTExtras.NBTToList(nbt.getList("allies", Tag.TAG_COMPOUND), NbtUtils::loadUUID));
			this.allyNames = new HashSet<>(NBTExtras.NBTToList(nbt.getList("allyNames", Tag.TAG_STRING), StringTag::getAsString));

			this.spellsDiscovered = new HashSet<>();
			for(int id : nbt.getIntArray("discoveredSpells")){
				spellsDiscovered.add(Spell.byMetadata(id));
			}

			this.maxTierReached = Tier.values()[nbt.getInt("maxTierReached")];

			// Probably won't be null but we may as well just reinitialise it instead of clearing it
			this.recentSpells = EvictingQueue.create(MAX_RECENT_SPELLS);
			this.recentSpells.addAll(NBTExtras.NBTToList(nbt.getList("recentSpells", Tag.TAG_INT),
					(IntTag tag) -> Spell.byMetadata(tag.getAsInt())));

			try{
				storedVariables.forEach(k -> this.spellData.put(k, k.read(nbt)));
			}catch(ClassCastException e){
				// Should only happen if someone manually edits the save file
				Wizardry.logger.error("Wizard data NBT tag was not of expected type!", e);
			}
		}
	}

	// ============================================== Event Handlers ==============================================

	@SubscribeEvent
	// The type parameter here has to be Entity, not EntityPlayer, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<Entity> event){

		if(event.getObject() instanceof Player)
			event.addCapability(new ResourceLocation(Wizardry.MODID, "WizardData"),
					new WizardData.Provider((Player)event.getObject()));
	}

	@SubscribeEvent
	public static void onPlayerCloneEvent(PlayerEvent.Clone event){

		WizardData newData = WizardData.get(event.getEntity());
		WizardData oldData = WizardData.get(event.getOriginal());

		newData.copyFrom(oldData, event.isWasDeath());

		newData.sync(); // In theory this should fix client/server discrepancies (see #69)
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinLevelEvent event){
		if(!event.getEntity().level.isClientSide && event.getEntity() instanceof ServerPlayer){
			// Synchronises wizard data after loading.
			WizardData data = WizardData.get((Player)event.getEntity());
			if(data != null) data.sync();
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){

		if(event.getEntity() instanceof Player){

			Player player = (Player)event.getEntity();

			if(WizardData.get(player) != null){
				WizardData.get(player).update();
			}
		}
	}

	// ========================================== Capability Boilerplate ==========================================

	/**
	 * This is a nested class for a few reasons: firstly, it makes sense because instances of this and WizardData go
	 * hand-in-hand; secondly, it's too short to be worth a separate file; and thirdly (and most importantly) it allows
	 * me to access WIZARD_DATA_CAPABILITY while keeping it private.
	 */
	public static class Provider implements ICapabilitySerializable<CompoundTag> {

        private final LazyOptional<WizardData> data;

        public Provider(Player player) {
            data = LazyOptional.of(() ->
            {
                WizardData i = new WizardData(player);
                return i;
            });
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
            return WIZARD_DATA_CAPABILITY.orEmpty(capability, data.cast());
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.orElseThrow(NullPointerException::new).serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
        }

	}

}
