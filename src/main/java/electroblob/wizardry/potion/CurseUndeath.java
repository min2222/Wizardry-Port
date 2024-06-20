package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CurseUndeath extends Curse {

	public CurseUndeath(MobEffectCategory category, int liquiidColour){
		super(category, liquiidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_undeath.png"));
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier){
		return true;
	}

	@Override
	public void applyEffectTick(LivingEntity entitylivingbase, int strength){

		// Adapted from Zombie
		if(entitylivingbase.level.isDay() && !entitylivingbase.level.isClientSide){

			float f = entitylivingbase.getLightLevelDependentMagicValue();

			if(f > 0.5F && entitylivingbase.level.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F
					&& entitylivingbase.level.canSeeSky(new BlockPos(entitylivingbase.getX(),
					entitylivingbase.getY() + (double)entitylivingbase.getEyeHeight(), entitylivingbase.getZ()))){

				boolean flag = true;
				ItemStack itemstack = entitylivingbase.getItemBySlot(EquipmentSlot.HEAD);

				if (!itemstack.isEmpty()) {
					if (itemstack.isDamageableItem()) {
						itemstack.setDamageValue(itemstack.getDamageValue() + entitylivingbase.level.random.nextInt(2));
						if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
							if (itemstack.getItem() instanceof ItemWizardArmour) {
								entitylivingbase.setSecondsOnFire(8);
							} else {
								entitylivingbase.broadcastBreakEvent(EquipmentSlot.HEAD);
								entitylivingbase.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
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
