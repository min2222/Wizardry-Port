package electroblob.wizardry.client.audio;

import electroblob.wizardry.item.ISpellCastingItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;

/** A special type of moving sound that stops when the player stops charging up a spell, either by releasing the mouse
 * button or when the charge-up time has elapsed. */
public class MovingSoundSpellCharge extends MovingSoundEntity<LivingEntity> {

	public MovingSoundSpellCharge(LivingEntity entity, SoundEvent sound, SoundSource category, float volume, float pitch, boolean repeat){
		super(entity, sound, category, volume, pitch, repeat);
	}

	@Override
	public void tick(){

		if(source.isUsingItem()){

			ItemStack stack = source.getUseItem();

			if(stack.getItem() instanceof ISpellCastingItem){

				if(source.getTicksUsingItem() < ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack).getChargeup()){
					super.tick();
					return;
				}
			}
		}

		this.stop();
	}

}
