package electroblob.wizardry.client.renderer.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class RenderBlinkEffect {

	/** The remaining time for which the blink screen overlay effect will be displayed in first-person. Since this is
	 * only for the first-person player (the instance of which is itself stored in a static variable), this can simply
	 * be stored statically here, rather than needing to be in {@code WizardData}. */
	private static int blinkEffectTimer;
	/** The number of ticks the blink effect lasts for. */
	private static final int BLINK_EFFECT_DURATION = 8;

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/blink_overlay.png");

	/** Starts the first-person blink overlay effect. */
	public static void playBlinkEffect(){
		if(Wizardry.settings.blinkEffect) blinkEffectTimer = BLINK_EFFECT_DURATION;
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getInstance().player && event.phase == TickEvent.Phase.END){

			if(Wizardry.settings.blinkEffect){
				if(blinkEffectTimer > 0) blinkEffectTimer--;
			}else{
				blinkEffectTimer = 0;
			}
		}
	}

	@SubscribeEvent
	public static void onFOVUpdateEvent(ViewportEvent.ComputeFov event){

		if(blinkEffectTimer > 0){
			float f = ((float)Math.max(blinkEffectTimer - 2, 0))/BLINK_EFFECT_DURATION;
            event.setFOV(event.getFOV() + f * f * 0.7f);
		}
	}

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGuiOverlayEvent.Post event){

    	if(event.getOverlay() == VanillaGuiOverlay.HELMET.type()) {

			if(blinkEffectTimer > 0){

				float alpha = ((float)blinkEffectTimer)/BLINK_EFFECT_DURATION;

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1, 1, 1, alpha);

                WizardryClientEventHandler.renderScreenOverlay(event.getPoseStack(), event.getWindow(), SCREEN_OVERLAY_TEXTURE);

                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
			}
		}
	}

}
