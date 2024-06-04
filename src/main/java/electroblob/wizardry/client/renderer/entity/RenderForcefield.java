package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.entity.construct.EntityForcefield;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;

public class RenderForcefield extends Render<EntityForcefield> {

	private static final float EXPANSION_TIME = 3;

	public RenderForcefield(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityForcefield entity, double x, double y, double z, float yaw, float partialTicks){

		// For now we're just using a UV sphere

		GlStateManager.pushMatrix();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		GlStateManager.translate(x, y, z);

		float latStep = (float)Math.PI/20;
		float longStep = (float)Math.PI/20;

		float pulse = Mth.sin((entity.tickCount + partialTicks)/10f);

		float r = 0.35f, g = 0.55f + 0.05f * pulse, b = 1;

		float radius = entity.getRadius();
		float a = 0.5f;

		if(entity.tickCount > entity.lifetime - EXPANSION_TIME){
			radius *= 1 + 0.2f * (entity.tickCount + partialTicks - (entity.lifetime - EXPANSION_TIME))/EXPANSION_TIME;
			a *= Math.max(0, 1 - (entity.tickCount + partialTicks - (entity.lifetime - EXPANSION_TIME))/EXPANSION_TIME);
		}else if(entity.tickCount < EXPANSION_TIME){
			radius *= 1 - (EXPANSION_TIME - entity.tickCount - partialTicks)/EXPANSION_TIME;
			a *= 1 - (EXPANSION_TIME - entity.tickCount - partialTicks)/EXPANSION_TIME;
		}

		// Draw the inside first
		drawSphere(radius - 0.1f - 0.025f * pulse, latStep, longStep, true, r, g, b, a);
		drawSphere(radius - 0.1f - 0.025f * pulse, latStep, longStep, false, 1, 1, 1, a);
		drawSphere(radius, latStep, longStep, false, r, g, b, 0.7f * a);

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityForcefield entity){
		return null;
	}

	/**
	 * Draws a sphere (using lat/long triangles) with the given parameters.
	 * @param radius The radius of the sphere.
	 * @param latStep The latitude step; smaller is smoother but increases performance cost.
	 * @param longStep The longitude step; smaller is smoother but increases performance cost.
	 * @param inside Whether to draw the outside or the inside of the sphere.
	 * @param r The red component of the sphere colour.
	 * @param g The green component of the sphere colour.
	 * @param b The blue component of the sphere colour.
	 * @param a The alpha component of the sphere colour.
	 */
	private static void drawSphere(float radius, float latStep, float longStep, boolean inside, float r, float g, float b, float a){

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		boolean goingUp = inside;

		buffer.pos(0, goingUp ? -radius : radius, 0).color(r, g, b, a).endVertex(); // Start at the north pole

		for(float longitude = -(float)Math.PI; longitude <= (float)Math.PI; longitude += longStep){

			// Leave the poles out since they only have a single point per stack instead of two
			for(float theta = (float)Math.PI/2 - latStep; theta >= -(float)Math.PI/2 + latStep; theta -= latStep){

				float latitude = goingUp ? -theta : theta;

				float hRadius = radius * Mth.cos(latitude);
				float vy = radius * Mth.sin(latitude);
				float vx = hRadius * Mth.sin(longitude);
				float vz = hRadius * Mth.cos(longitude);

				buffer.pos(vx, vy, vz).color(r, g, b, a).endVertex();

				vx = hRadius * Mth.sin(longitude + longStep);
				vz = hRadius * Mth.cos(longitude + longStep);

				buffer.pos(vx, vy, vz).color(r, g, b, a).endVertex();
			}

			// The next pole
			buffer.pos(0, goingUp ? radius : -radius, 0).color(r, g, b, a).endVertex();

			goingUp = !goingUp;
		}

		tessellator.draw();
	}

}
