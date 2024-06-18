package electroblob.wizardry.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Same as {@link PotionMagicEffect}, but also implements {@link ICustomPotionParticles} to allow anonymous classes to
 * extend it and add their own particles. It is advised that all other (named) classes extend and implement the
 * underlying class and interface rather than extending this class.
 */
public abstract class PotionMagicEffectParticles extends PotionMagicEffect implements ICustomPotionParticles {

	public PotionMagicEffectParticles(MobEffectCategory category, int liquidColour, ResourceLocation texture){
		super(category, liquidColour, texture);
	}

}
