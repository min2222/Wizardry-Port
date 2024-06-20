package electroblob.wizardry.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemFlamingAxe extends AxeItem implements IConjuredItem {

	private Rarity rarity = Rarity.COMMON;

	public ItemFlamingAxe(Tier material){
		super(material, 8, -3, new Item.Properties().durability(1200).setNoRepair()); // Might cause problems if removed, the actual number is irrelevant as long as it's > 0
		addAnimationPropertyOverrides();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack){

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        builder.putAll(multimap);
		if(slot == EquipmentSlot.MAINHAND){
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(POTENCY_MODIFIER,
					"Potency modifier", IConjuredItem.getDamageMultiplier(stack) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
		}

		return builder.build();
	}

	public Item setRarity(Rarity rarity){
		this.rarity = rarity;
		return this;
	}

	@Override
	public Rarity getRarity(ItemStack stack){
		return rarity;
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.flaming_axe);
	}

	@Override
	public int getBarColor(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// tick() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getDamageValue();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setDamageValue(damage + 1);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot){
		attackDamageBaseline = Spells.flaming_axe.getProperty(Spell.DAMAGE).floatValue();
		return super.getDefaultAttributeModifiers(equipmentSlot);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity wielder){
		if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
			target.setSecondsOnFire(Spells.flaming_axe.getProperty(Spell.BURN_DURATION).intValue());
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
		return true;
	}

	@Override
	public boolean isRepairable(ItemStack stack){
		return false;
	}

	@Override
	public int getEnchantmentValue(){
		return 0;
	}

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	// Cannot be dropped
	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player){
		return false;
	}

}
