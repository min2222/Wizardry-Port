package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ISpecialArmor;

public class CurseUndeath extends Curse {

	public CurseUndeath(boolean isBadEffect, int liquiidColour){
		super(isBadEffect, liquiidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_undeath.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":curse_of_undeath");
	}

	@Override
	public boolean isReady(int duration, int amplifier){
		return true;
	}

	@Override
	public void performEffect(LivingEntity entitylivingbase, int strength){

		// Adapted from EntityZombie
		if(entitylivingbase.world.isDaytime() && !entitylivingbase.level.isClientSide){

			float f = entitylivingbase.getBrightness();

			if(f > 0.5F && entitylivingbase.world.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F
					&& entitylivingbase.world.canSeeSky(new BlockPos(entitylivingbase.getX(),
					entitylivingbase.getY() + (double)entitylivingbase.getEyeHeight(), entitylivingbase.getZ()))){

				boolean flag = true;
				ItemStack itemstack = entitylivingbase.getItemStackFromSlot(EquipmentSlot.HEAD);

				if (!itemstack.isEmpty()) {
					if (itemstack.isItemStackDamageable()) {

						if (itemstack.getItem() instanceof ISpecialArmor) {
							((ISpecialArmor) itemstack.getItem()).damageArmor(entitylivingbase, itemstack, DamageSource.ON_FIRE, entitylivingbase.world.random.nextInt(2), EquipmentSlot.HEAD.getSlotIndex());
						} else {
							itemstack.setItemDamage(itemstack.getItemDamage() + entitylivingbase.world.random.nextInt(2));
							if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) {
								if (itemstack.getItem() instanceof ItemWizardArmour) {
									entitylivingbase.setSecondsOnFire(8);
								} else {
									entitylivingbase.renderBrokenItemStack(itemstack);
									entitylivingbase.setItemStackToSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
								}
							}
						}
					}

					flag = false;
				}

				if(flag){
					entitylivingbase.setSecondsOnFire(8);
				}
			}
		}
	}
}
