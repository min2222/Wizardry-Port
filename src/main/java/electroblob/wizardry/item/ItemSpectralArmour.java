package electroblob.wizardry.item;

import electroblob.wizardry.registry.Spells;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemSpectralArmour extends ArmorItem implements IConjuredItem {

	public ItemSpectralArmour(ArmorMaterial material, EquipmentSlot armourType){
		super(material, renderIndex, armourType);
		setCreativeTab(null);
		setMaxDamage(1200);
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.conjure_armour);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return IConjuredItem.getTimerBarColour(stack);
	}

	// Overridden to stop the enchantment trick making the name turn blue.
	@Override
	public Rarity getRarity(ItemStack stack){
		return Rarity.COMMON;
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
	public void onArmorTick(Level world, Player player, ItemStack stack){
		int damage = stack.getItemDamage();
		if(damage > stack.getMaxDamage()) player.inventory.clearMatchingItems(this, -1, 1, null);
		stack.setItemDamage(damage + 1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack stack){
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

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type){

		if(slot == EquipmentSlot.LEGS) return "ebwizardry:textures/armour/spectral_armour_legs.png";

		return "ebwizardry:textures/armour/spectral_armour.png";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public net.minecraft.client.model.ModelBiped getArmorModel(LivingEntity entityLiving, ItemStack itemStack,
                                                               EquipmentSlot armorSlot, net.minecraft.client.model.ModelBiped _default){
		net.minecraft.client.renderer.GlStateManager.enableBlend();
		net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
				net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
				net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE,
				net.minecraft.client.renderer.GlStateManager.DestFactor.ZERO
		);
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}

}
