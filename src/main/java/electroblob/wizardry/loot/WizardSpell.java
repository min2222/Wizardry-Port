package electroblob.wizardry.loot;

import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryLoot;
import electroblob.wizardry.spell.Spell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function that allows spell books to select a random spell from the spells used by the ISpellCaster that dropped
 * them.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public class WizardSpell extends LootItemConditionalFunction {

	protected WizardSpell(LootItemCondition[] conditions){
		super(conditions);
	}
	
    @Override
    public LootItemFunctionType getType() {
        return WizardryLoot.WIZARD_SPELL.get();
    }

	@Override
	public ItemStack run(ItemStack stack, LootContext context){
		
		RandomSource random = context.getRandom();
		if(!(stack.getItem() instanceof ItemSpellBook) && !(stack.getItem() instanceof ItemScroll)) Wizardry.logger
				.warn("Applying the wizard_spell loot function to an item that isn't a spell book or scroll.");

		if(context.getParam(LootContextParams.THIS_ENTITY) instanceof ISpellCaster){
			List<Spell> spells = ((ISpellCaster)context.getParam(LootContextParams.THIS_ENTITY)).getSpells();
			spells.remove(Spells.MAGIC_MISSILE); // Can't drop magic missile
			spells.removeIf(s -> !s.applicableForItem(stack.getItem()));
			if(spells.isEmpty()){
				Wizardry.logger.warn("Tried to apply the wizard_spell loot function to an item, but none of the looted entity's spells were applicable for that item. This is probably a bug!");
			}else{
                CompoundTag tag = new CompoundTag();
                tag.putInt("Spell", spells.get(random.nextInt(spells.size())).metadata());
                stack.addTagElement("Spells", tag);
			}
		}else{
			Wizardry.logger.warn("Applying the wizard_spell loot function to an entity that isn't a spell caster.");
		}

		return stack;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<WizardSpell> {

		public Serializer(){
			super();
		}

		public void serialize(JsonObject object, WizardSpell function, JsonSerializationContext serializationContext){

		}

		public WizardSpell deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootItemCondition[] conditions){
			return new WizardSpell(conditions);
		}
	}

}
