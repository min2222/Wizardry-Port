package electroblob.wizardry.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * As of Wizardry 4.2, this class is used by all of wizardry's potions. Potions that work solely on events
 * instantiate this class directly, all other potions extend it.
 */
public class PotionMagicEffect extends MobEffect {

	private final ResourceLocation texture;

	public PotionMagicEffect(boolean isBadEffect, int liquidColour, ResourceLocation texture){
		super(isBadEffect, liquidColour);
		this.texture = texture;
	}

	@Override
	public void performEffect(LivingEntity entitylivingbase, int strength){
		// Nothing here because this potion works on events.
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInventoryEffect(int x, int y, MobEffectInstance effect, net.minecraft.client.Minecraft mc){
		drawIcon(x + 6, y + 7, effect, mc);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderHUDEffect(int x, int y, MobEffectInstance effect, net.minecraft.client.Minecraft mc, float alpha){
		net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, alpha);
		drawIcon(x + 3, y + 3, effect, mc);
	}
	
	@OnlyIn(Dist.CLIENT)
	protected void drawIcon(int x, int y, MobEffectInstance effect, net.minecraft.client.Minecraft mc){
		mc.renderEngine.bindTexture(texture);
		electroblob.wizardry.client.DrawingUtils.drawTexturedRect(x, y, 0, 0, 18, 18, 18, 18);
	}

}
