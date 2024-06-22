package electroblob.wizardry.client.renderer.tileentity;

import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;

import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RenderImbuementAltar implements BlockEntityRenderer<TileEntityImbuementAltar> {

	public RenderImbuementAltar(BlockEntityRendererProvider.Context ctx){}

	@Override
	public void render(TileEntityImbuementAltar tileentity, float partialTicks, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {
		p_112309_.pushPose();

		p_112309_.translate(0.5F, 1.4F, 0.5F);
		p_112309_.mulPose(Vector3f.ZP.rotationDegrees(180));

		float t = Minecraft.getInstance().player.tickCount + partialTicks;
		p_112309_.translate(0, 0.05f * Mth.sin(t/15), 0);

		this.renderItem(p_112309_, tileentity, t, p_112310_, p_112311_);
		this.renderRays(p_112309_, tileentity, partialTicks);

		p_112309_.popPose();
	}

	private void renderItem(PoseStack posestack, TileEntityImbuementAltar tileentity, float t, MultiBufferSource source, int light) {

		ItemStack stack = tileentity.getStack();

		if(!stack.isEmpty()){

			posestack.pushPose();

			posestack.mulPose(Vector3f.XP.rotationDegrees(180));
			posestack.mulPose(Vector3f.YP.rotationDegrees(t));
			posestack.scale(0.85F, 0.85F, 0.85F);

			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.FIXED, light, OverlayTexture.NO_OVERLAY, posestack, source, 0);

			posestack.popPose();
		}
	}

	private void renderRays(PoseStack stack, TileEntityImbuementAltar tileentity, float partialTicks){

		float t = Minecraft.getInstance().player.tickCount + partialTicks;

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		Random random = new Random(tileentity.getBlockPos().asLong()); // Use position to get a constant seed

		int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(tileentity.getDisplayElement());

		if(colours == null) return; // Shouldn't happen

		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.depthMask(false);

		int r1 = colours[1] >> 16 & 255;
		int g1 = colours[1] >> 8 & 255;
		int b1 = colours[1] & 255;

		int r2 = colours[2] >> 16 & 255;
		int g2 = colours[2] >> 8 & 255;
		int b2 = colours[2] & 255;

		for(int j = 0; j < 30; j++){

			int m = random.nextInt(10);
			int n = random.nextInt(10);

			int sliceAngle = 20 + m;
			float scale = 0.5f;

			stack.pushPose();

			float progress = Math.min(tileentity.getImbuementProgress() + partialTicks/141, 1);
			float s = 1 - progress;
			s = 1 - s*s;
			stack.scale(s, s, s);

			// TODO: This needs optimising! We should easily be able to do this with a single draw() call
			// Same for magic light and black hole, which don't need ray textures either!
			stack.mulPose(Vector3f.XP.rotationDegrees(31 * m));
			stack.mulPose(Vector3f.ZP.rotationDegrees(31 * n));

			buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

			float fade = (Math.min(1, 1.9f - progress) - 0.9f) * 10;

			buffer.vertex(0, 0, 0).color(r1, g1, b1, (int)(255 * fade)).endVertex();
			buffer.vertex(0, 0, 0).color(r1, g1, b1, (int)(255 * fade)).endVertex();

			double x1 = scale * Mth.sin((t + 40 * j) * ((float)Math.PI / 180));
			double z1 = scale * Mth.cos((t + 40 * j) * ((float)Math.PI / 180));

			double x2 = scale * Mth.sin((t + 40 * j - sliceAngle) * ((float)Math.PI / 180));
			double z2 = scale * Mth.cos((t + 40 * j - sliceAngle) * ((float)Math.PI / 180));

			buffer.vertex(x1, 0, z1).color(r2, g2, b2, 0).endVertex();
			buffer.vertex(x2, 0, z2).color(r2, g2, b2, 0).endVertex();

			BufferUploader.drawWithShader(buffer.end());

			stack.popPose();
		}

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}

}
