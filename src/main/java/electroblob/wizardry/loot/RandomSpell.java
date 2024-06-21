package electroblob.wizardry.loot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryLoot;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function that allows spell books and scrolls to select a random spell based on the standard weighting.
 * Can be used as-is with no parameters, but several optional parameters are available for those wishing to customise further:
 * <p></p>
 * - <b>spells</b>: A list of spells to choose from. Defaults to all enabled spells.<br>
 * - <b>ignore_weighting</b>: true to ignore the standard weighting and just pick a completely random spell. Defaults to
 * false.<br>
 * - <b>undiscovered_bias</b>: A number between 0 and 1 representing the bias towards undiscovered spells, with 0
 * being no bias and 1 meaning spells are guaranteed to be undiscovered.<br>
 * - <b>tiers</b>: A list of tiers to choose from. Defaults to all tiers.<br>
 * - <b>elements</b>: A list of elements to choose from. Defaults to all elements.
 * <p></p>
 * This class is effectively a loot table-friendly replacement for the standard weighting method in WizardryUtilities
 * which was the basis of all the old loot systems (not counting wizard trades, which were - and still are - completely
 * separate).
 * <p></p>
 * Since spells are stored as metadata, this <i>could</i> be done by having an entry for each tier and letting it pick a
 * random spell from that tier by setting the metadata to a random value in the range of values that correspond to that
 * tier. However, this creates a two-fold problem: firstly, not all spells are in tier order, and secondly, if spells
 * are added via addon mods the entries would have to be updated manually with the new numbers.
 * <p></p>
 * (This reasoning is similar to that for the enchant_randomly function in vanilla Minecraft, since you can specify NBT
 * data in loot tables, but using NBT in this way would be incredibly verbose and inflexible, hence the loot function.)
 *
 * @author Electroblob
 * @since Wizardry 1.2
 */
/* You could even use the yet more long-winded method of adding each spell as an individual entry, which would be
 * stupidly long but would allow precise control over each individual spell... I'll leave that for pack makers to do if
 * they have the patience! */
public class RandomSpell extends LootItemConditionalFunction {

	private final List<Spell> spells;
	private final boolean ignoreWeighting;
	private final float undiscoveredBias;
	private final List<Tier> tiers;
	private final List<Element> elements;

	protected RandomSpell(LootItemCondition[] conditions, List<Spell> spells, boolean ignoreWeighting,
						  float undiscoveredBias, List<Tier> tiers, List<Element> elements){
		super(conditions);
		this.spells = spells;
		this.ignoreWeighting = ignoreWeighting;
		this.undiscoveredBias = undiscoveredBias;
		this.tiers = tiers;
		this.elements = elements;
	}
	
    @Override
    public LootItemFunctionType getType() {
        return WizardryLoot.RANDOM_SPELL.get();
    }

	@Override
	public ItemStack run(ItemStack stack, LootContext context){

		RandomSource random = context.getRandom();
		if(!(stack.getItem() instanceof ItemSpellBook) && !(stack.getItem() instanceof ItemScroll)) Wizardry.logger
				.warn("Applying the random_spell loot function to an item that isn't a spell book or scroll.");

		SpellProperties.Context spellContext = context.getParam(LootContextParams.THIS_ENTITY) == null ? SpellProperties.Context.TREASURE
				: SpellProperties.Context.LOOTING;

		// This method is badly-named, loot chests pass a player through too, not just mobs
		// (And WHY does it only return an entity?! The underlying field is always a player so I'm casting it anyway)
        Player player = context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);

		Spell spell = pickRandomSpell(stack, random, spellContext, player);

		if(spell == Spells.NONE) Wizardry.logger.warn("Tried to apply the random_spell loot function to an item, but no"
					+ " enabled spells matched the criteria specified. Substituting placeholder (metadata 0) item.");

        CompoundTag tag = new CompoundTag();
        tag.putInt("Spell", spell.metadata());
        stack.addTagElement("Spells", tag);

		return stack;
	}

	private Spell pickRandomSpell(ItemStack stack, RandomSource random, SpellProperties.Context spellContext, Player player){

		// We're now doing this first because we need to know which spells we have to play with before selecting a tier and element
		List<Spell> possibleSpells = Spell.getSpells(s -> s.isEnabled(spellContext) && s.applicableForItem(stack.getItem())
				// Remove excluded tiers/elements immediately (mainly because empty tier checks should account for excluded elements)
				&& (tiers == null || tiers.contains(s.getTier())) && (elements == null || elements.contains(s.getElement())));

		if(spells != null && !spells.isEmpty()){
			possibleSpells.retainAll(spells); // Normally you wouldn't specify a spells list AND tiers/elements... but you could!
		}

		if(stack.getItem() instanceof ItemScroll) possibleSpells.removeIf(s -> !s.isEnabled(SpellProperties.Context.SCROLL));
		if(stack.getItem() instanceof ItemSpellBook) possibleSpells.removeIf(s -> !s.isEnabled(SpellProperties.Context.BOOK));

		// Select a tier...

		List<Tier> possibleTiers = new ArrayList<>();

		if(tiers == null || tiers.isEmpty()){
			possibleTiers.addAll(Arrays.asList(Tier.values()));
		}else{
			possibleTiers.addAll(tiers);
		}
		// Remove all empty tiers
		possibleTiers.removeIf(t -> possibleSpells.stream().noneMatch(s -> s.getTier() == t)); // Lambdaception!

		if(possibleTiers.isEmpty()) return Spells.NONE; // Gotta disable a lot of spells for this to happen

		Tier tier = ignoreWeighting ? possibleTiers.get(random.nextInt(possibleTiers.size()))
				: Tier.getWeightedRandomTier(random, possibleTiers.toArray(new Tier[0]));

		// Remove all spells that aren't of the selected tier
		possibleSpells.removeIf(s -> s.getTier() != tier);
		if(possibleSpells.isEmpty()) return Spells.NONE; // Should be caught by the no-elements check but we might as well

		// Select an element...

		List<Element> possibleElements = new ArrayList<>();

		// Elements aren't weighted
		if(elements == null || elements.isEmpty()){
			possibleElements.addAll(Arrays.asList(Element.values()));
		}else{
			possibleElements.addAll(elements);
		}
		// Remove all empty elements
		possibleElements.removeIf(e -> possibleSpells.stream().noneMatch(s -> s.getElement() == e));

		if(possibleElements.isEmpty()) return Spells.NONE; // A bit more likely I guess, but still pretty unlikely

		Element element = possibleElements.get(random.nextInt(possibleElements.size()));

		// Remove all spells that aren't of the selected tier
		possibleSpells.removeIf(s -> s.getElement() != element);
		if(possibleSpells.isEmpty()) return Spells.NONE; // If it fails anywhere, it'll most likely be here

		if(player != null){

			float bias = undiscoveredBias;
			// Archivist's eyeglass increases undiscovered bias by 0.4 up to a maximum of 0.9
			if(ItemArtefact.isArtefactActive(player, WizardryItems.CHARM_SPELL_DISCOVERY.get()))
				bias = Math.min(bias + 0.4f, 0.9f);

			// Remove either the undiscovered spells or the discovered ones, depending on the bias
			if(bias > 0){

				WizardData data = WizardData.get(player);

				int discoveredCount = (int)possibleSpells.stream().filter(data::hasSpellBeenDiscovered).count();
				// If none have been discovered or they've all been discovered, don't bother!
				if(discoveredCount > 0 && discoveredCount < possibleSpells.size()){
					// Kinda unintuitive but it's very neat!
					boolean keepDiscovered = random.nextFloat() > 0.5f + 0.5f * bias;
					possibleSpells.removeIf(s -> keepDiscovered != data.hasSpellBeenDiscovered(s));
				}
			}
		}

		return possibleSpells.get(random.nextInt(possibleSpells.size())); // Finally pick a spell
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<RandomSpell> {

		public void serialize(JsonObject object, RandomSpell function, JsonSerializationContext serializationContext){

			if(function.spells != null && !function.spells.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Spell spell : function.spells){
					jsonarray.add(new JsonPrimitive(spell.getRegistryName().toString()));
				}

				object.add("spells", jsonarray);
			}

			object.addProperty("ignore_weighting", function.ignoreWeighting);

			object.addProperty("undiscovered_bias", function.undiscoveredBias);

			if(function.tiers != null && !function.tiers.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Tier tier : function.tiers){
					jsonarray.add(new JsonPrimitive(tier.getUnlocalisedName()));
				}

				object.add("tiers", jsonarray);
			}

			if(function.elements != null && !function.elements.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Element element : function.elements){
					jsonarray.add(new JsonPrimitive(element.getName()));
				}

				object.add("elements", jsonarray);
			}
		}

		public RandomSpell deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootItemCondition[] conditions){

			List<Spell> spells = null;
			List<Tier> tiers = null;
			List<Element> elements = null;

			if(object.has("spells")){

				spells = new ArrayList<>();

				// Importantly, it is necessary to specify a default (the new JsonArray) here because otherwise the
				// parameter will be mandatory, and the game will crash if it isn't present.
				for(JsonElement element : GsonHelper.getAsJsonArray(object, "spells", new JsonArray())){

					String string = GsonHelper.convertToString(element, "spell");

					Spell spell = Spell.get(string);

					if(spell == null){
						throw new JsonSyntaxException("Unknown spell \'" + string + "\'");
					}

					spells.add(spell);
				}
			}

			boolean ignoreWeighting = GsonHelper.getAsBoolean(object, "ignore_weighting", false);

			float undiscoveredBias = GsonHelper.getAsFloat(object, "undiscovered_bias", 0);

			if(object.has("tiers")){

				tiers = new ArrayList<>();

				for(JsonElement element : GsonHelper.getAsJsonArray(object, "tiers", new JsonArray())){

					String string = GsonHelper.convertToString(element, "tier");

					try {
						tiers.add(Tier.fromName(string));
					}catch(IllegalArgumentException e){
						// If the string does not match any of the tiers, throws an exception.
						throw new JsonSyntaxException("Unknown tier \'" + string + "\'");
					}
				}
			}

			if(object.has("elements")){

				elements = new ArrayList<>();

				for(JsonElement jelement : GsonHelper.getAsJsonArray(object, "elements", new JsonArray())){

					String string = GsonHelper.convertToString(jelement, "element");

					try {
						elements.add(Element.fromName(string));
					}catch(IllegalArgumentException e){
						// If the string does not match any of the elements, throws an exception.
						throw new JsonSyntaxException("Unknown element \'" + string + "\'");
					}
				}
			}

			return new RandomSpell(conditions, spells, ignoreWeighting, undiscoveredBias, tiers, elements);
		}
	}

}
