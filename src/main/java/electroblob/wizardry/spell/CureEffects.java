package electroblob.wizardry.spell;

import java.util.ArrayList;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CureEffects extends SpellBuff {

	public CureEffects(){
		super("cure_effects", 0.8f, 0.8f, 1);
		this.soundValues(0.7f, 1.2f, 0.4f);
	}
	
	@Override
	protected boolean applyEffects(LivingEntity caster, SpellModifiers modifiers){

		if(!caster.getActiveEffects().isEmpty()){

			ItemStack milk = new ItemStack(Items.MILK_BUCKET);

			boolean flag = false;

			for(MobEffectInstance effect : new ArrayList<>(caster.getActiveEffects())){ // Get outta here, CMEs
				// The PotionEffect version (as opposed to Potion) does not call cleanup callbacks
				if(effect.isCurativeItem(milk)){
					caster.removeEffect(effect.getEffect());
					flag = true;
				}
			}
			
			return flag;
		}
		
		return false;
	}

}
