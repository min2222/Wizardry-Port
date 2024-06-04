package electroblob.wizardry.item;

import com.google.common.collect.Multimap;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemFrostAxe extends ItemAxe implements IConjuredItem {

	private Rarity rarity = Rarity.COMMON;

	public ItemFrostAxe(ToolMaterial material){
		super(material, 8, -3);
		setMaxDamage(1200);
		setNoRepair();
		setCreativeTab(null);
		addAnimationPropertyOverrides();
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack){

		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(slot);

		if(slot == EntityEquipmentSlot.MAINHAND){
			multimap.put(Attributes.ATTACK_DAMAGE.getName(), new AttributeModifier(POTENCY_MODIFIER,
					"Potency modifier", IConjuredItem.getDamageMultiplier(stack) - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
		}

		return multimap;
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
		return this.getMaxDamageFromNBT(stack, Spells.frost_axe);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	@Override
	// This method allows the code for the item's timer to be greatly simplified by damaging it directly from
	// onUpdate() and removing the workaround that involved WizardData and all sorts of crazy stuff.
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged) return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void onUpdate(ItemStack stack, Level world, Entity entity, int slot, boolean selected){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) InventoryUtils.replaceItemInInventory(entity, slot, stack, ItemStack.EMPTY);
		stack.setItemDamage(damage + 1);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot){
		attackDamage = Spells.frost_axe.getProperty(Spell.DAMAGE).floatValue();
		return super.getItemAttributeModifiers(equipmentSlot);
	}

	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity wielder){
		if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
			target.addPotionEffect(new MobEffectInstance(WizardryPotions.frost, 160, 1));
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack par2ItemStack){
		return false;
	}

	@Override
	public int getItemEnchantability(){
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
