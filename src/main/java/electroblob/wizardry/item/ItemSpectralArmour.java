package electroblob.wizardry.item;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import electroblob.wizardry.registry.Spells;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class ItemSpectralArmour extends ArmorItem implements IConjuredItem {

	public ItemSpectralArmour(ArmorMaterial material, EquipmentSlot armourType){
		super(material, armourType, new Item.Properties().durability(1200));
	}

	@Override
	public int getMaxDamage(ItemStack stack){
		return this.getMaxDamageFromNBT(stack, Spells.CONJURE_ARMOUR);
	}

	@Override
	public int getBarColor(ItemStack stack){
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
	public void onArmorTick(ItemStack stack, Level world, Player player){
		int damage = stack.getDamageValue();
		if(damage > stack.getMaxDamage()) 
			player.getInventory().clearOrCountMatchingItems((item) -> { 
			return item.is(this);
		}, 1, player.inventoryMenu.getCraftSlots());
		stack.setDamageValue(damage + 1);
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

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type){

		if(slot == EquipmentSlot.LEGS) return "ebwizardry:textures/armour/spectral_armour_legs.png";

		return "ebwizardry:textures/armour/spectral_armour.png";
	}
	

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    	consumer.accept(new IClientItemExtensions() {
    		@Override
    		public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
    				EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
    			RenderSystem.enableBlend();
    			RenderSystem.blendFuncSeparate(
    					GlStateManager.SourceFactor.SRC_ALPHA,
    					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
    					GlStateManager.SourceFactor.ONE,
    					GlStateManager.DestFactor.ZERO);
    			return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
    		}
    	});
    }

}
