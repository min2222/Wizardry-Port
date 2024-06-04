package electroblob.wizardry.client.renderer.overlay;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(Dist.CLIENT)
public class RenderFrostOverlay {

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_overlay.png");

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET){

			if(Minecraft.getMinecraft().player.hasEffect(WizardryPotions.frost)){

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableAlpha();

				WizardryClientEventHandler.renderScreenOverlay(event.getResolution(), SCREEN_OVERLAY_TEXTURE);

				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}

}
