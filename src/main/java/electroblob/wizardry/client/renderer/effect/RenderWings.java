package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderWings {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/wing.png");

	// No first person in here because you can never see the wings on your back!

	// Third person
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event){

		Player player = event.getEntity();

		if(EntityUtils.isCasting(player, Spells.flight)){

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(event.getX(), event.getY(), event.getZ());

			// GlStateManager.rotate(-entityplayer.rotationYawHead, 0, 1, 0);
			GlStateManager.rotate(-player.renderYawOffset, 0, 1, 0);
			// GlStateManager.rotate(180, 1, 0, 0);

			Minecraft.getInstance().renderEngine.bindTexture(TEXTURE);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.translate(0.1, 0.4, -0.15);
			GlStateManager.rotate(20 + 20 * Mth.sin((player.tickCount + event.getPartialRenderTick()) * 0.3f), 0, 1, 0);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();

			GlStateManager.translate(-0.1, 0.4, -0.15);
			GlStateManager.rotate(-200 - 20 * Mth.sin((player.tickCount + event.getPartialRenderTick()) * 0.3f), 0, 1, 0);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();
		}
	}

}
