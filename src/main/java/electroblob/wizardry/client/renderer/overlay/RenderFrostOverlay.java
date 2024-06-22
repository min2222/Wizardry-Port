package electroblob.wizardry.client.renderer.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class RenderFrostOverlay {

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_overlay.png");

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGuiOverlayEvent.Post event){

    	if(event.getOverlay() == VanillaGuiOverlay.HELMET.type()) {

			if(Minecraft.getInstance().player.hasEffect(WizardryPotions.FROST.get())){

            	RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1, 1, 1, 1);

				WizardryClientEventHandler.renderScreenOverlay(event.getPoseStack(), event.getWindow(), SCREEN_OVERLAY_TEXTURE);

                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
			}
		}
	}

}
