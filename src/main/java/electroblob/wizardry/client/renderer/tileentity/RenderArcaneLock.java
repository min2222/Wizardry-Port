package electroblob.wizardry.client.renderer.tileentity;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderArcaneLock {

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[8];

	static {
		for(int i = 0; i< TEXTURES.length; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/blocks/arcane_lock_" + i + ".png");
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderLevelLastEvent event){

		Player player = Minecraft.getInstance().player;
		Level world = Minecraft.getInstance().level;
		Vec3 origin = player.getEyePosition(event.getPartialTick());
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();

		boolean flag = false;
		boolean lighting = false;

		// Someone managed to get a CME here so let's iterate manually to be safe (don't copy the list, it's expensive!)
		// It's only cosmetic so if a tileentity somehow gets removed while we're rendering them it's not a big deal
		for(int i=0; i<world.loadedTileEntityList.size(); i++){

			BlockEntity tileentity = world.loadedTileEntityList.get(i);

			if(tileentity == null) continue; // What the heck VoxelMap

			if(tileentity.distanceToSqr(origin.x, origin.y, origin.z) <= tileentity.getMaxRenderDistanceSquared()
					&& tileentity.getTileData().hasUUID(ArcaneLock.NBT_KEY)){

				if(!flag){

					flag = true;

					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
					GlStateManager.disableLighting();
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

					GlStateManager.translate(-origin.x, -origin.y + player.getEyeHeight(), -origin.z);

					GlStateManager.color(1, 1, 1, 1);

					Minecraft.getInstance().renderEngine.bindTexture(TEXTURES[(player.tickCount % (TEXTURES.length * 2))/2]);

					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				}

				Vec3[] vertices = GeometryUtils.getVertices(level.getBlockState(tileentity.getPos()).getBoundingBox(world, tileentity.getPos()).grow(0.05).offset(tileentity.getPos()));

				drawFace(buffer, vertices[0], vertices[1], vertices[3], vertices[2], 0, 0, 1, 1); // Bottom
				drawFace(buffer, vertices[6], vertices[7], vertices[2], vertices[3], 0, 0, 1, 1); // South
				drawFace(buffer, vertices[5], vertices[6], vertices[1], vertices[2], 0, 0, 1, 1); // East
				drawFace(buffer, vertices[4], vertices[5], vertices[0], vertices[1], 0, 0, 1, 1); // North
				drawFace(buffer, vertices[7], vertices[4], vertices[3], vertices[0], 0, 0, 1, 1); // West
				drawFace(buffer, vertices[5], vertices[4], vertices[6], vertices[7], 0, 0, 1, 1); // Top

			}
		}

		if(flag){

			tessellator.draw();

			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
			if(lighting){
				GlStateManager.enableLighting();
			}
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}

	}

	private static void drawFace(BufferBuilder buffer, Vec3 topLeft, Vec3 topRight, Vec3 bottomLeft, Vec3 bottomRight, float u1, float v1, float u2, float v2){
		buffer.vertex(topLeft.x, topLeft.y, topLeft.z).uv(u1, v1).endVertex();
		buffer.vertex(topRight.x, topRight.y, topRight.z).uv(u2, v1).endVertex();
		buffer.vertex(bottomRight.x, bottomRight.y, bottomRight.z).uv(u2, v2).endVertex();
		buffer.vertex(bottomLeft.x, bottomLeft.y, bottomLeft.z).uv(u1, v2).endVertex();
	}

}
