package electroblob.wizardry.client.renderer.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockMagicLight;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class RenderMagicLight implements BlockEntityRenderer<TileEntityTimer> {

	private static final ResourceLocation RAY_TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/entity/light/ray.png");
	private static final ResourceLocation FLARE_TEXTURE = new ResourceLocation(Wizardry.MODID,
			"textures/entity/light/flare.png");
	
    public RenderMagicLight(BlockEntityRendererProvider.Context ctx) {

    }

	@Override
	public void render(TileEntityTimer tileentity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

		pPoseStack.pushPose();

		RenderSystem.disableCull();
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);

		pPoseStack.translate(0.5, 0.5, 0.5);

		float s = DrawingUtils.smoothScaleFactor(tileentity.getLifetime(), tileentity.timer, pPartialTick, 10, 10);
		pPoseStack.scale(s, s, s);

		// Renders the aura effect

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();

		pPoseStack.pushPose();

		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		// This counteracts the reverse rotation behaviour when in front f5 view.
		// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
		pPoseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		pPoseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		RenderSystem.setShaderTexture(0, FLARE_TEXTURE);

		buffer.vertex(pPoseStack.last().pose(), -0.6f, 0.6f, 0).uv(0, 0).endVertex();
		buffer.vertex(pPoseStack.last().pose(), 0.6f, 0.6f, 0).uv(1, 0).endVertex();
		buffer.vertex(pPoseStack.last().pose(), 0.6f, -0.6f, 0).uv(1, 1).endVertex();
		buffer.vertex(pPoseStack.last().pose(), -0.6f, -0.6f, 0).uv(0, 1).endVertex();

		BufferUploader.drawWithShader(buffer.end());

		pPoseStack.popPose();

		// Renders the rays

		// For some reason, the old blend function (GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA) caused the innermost
		// ends of the rays to appear black, so I have changed it to this, which looks very slightly different.
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.SRC_ALPHA);

		RenderSystem.setShaderTexture(0, RAY_TEXTURE);

		if(tileentity.randomiser.length >= 30){
			for(int j = 0; j < 30; j++){

				int sliceAngle = 20 + tileentity.randomiser[j];
				float scale = 0.5f;

				pPoseStack.pushPose();

				pPoseStack.mulPose(Vector3f.XP.rotationDegrees(31 * tileentity.randomiser[j]));
				pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(31 * tileentity.randomiser2[j]));

				/* OK, so here are the changes to rendering as far as I know: Vertex formats specify how the methods are
				 * arranged Color has to be called for every vertex, I think. The new methods thing is a bit weird,
				 * because other than the number of arguments there is essentially no difference between pos, tex,
				 * color, normal and lightmap. At least they make the code more readable. */
				
				buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX_COLOR);

				buffer.vertex(pPoseStack.last().pose(), 0, 0, 0).uv(0, 0).color(255, 255, 255, 0).endVertex();
				buffer.vertex(pPoseStack.last().pose(), 0, 0, 0).uv(0, 1).color(255, 255, 255, 0).endVertex();

				double x1 = scale * Mth.sin((tileentity.timer + 40 * j) * ((float)Math.PI / 180));
				// double y1 = 0.7*MathHelper.cos((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
				double z1 = scale * Mth.cos((tileentity.timer + 40 * j) * ((float)Math.PI / 180));

				double x2 = scale * Mth.sin((tileentity.timer + 40 * j - sliceAngle) * ((float)Math.PI / 180));
				// double y2 = 0.7*MathHelper.sin((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
				double z2 = scale * Mth.cos((tileentity.timer + 40 * j - sliceAngle) * ((float)Math.PI / 180));

				buffer.vertex(pPoseStack.last().pose(), (float)x1, 0, (float)z1).uv(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.vertex(pPoseStack.last().pose(), (float)x2, 0, (float)z2).uv(1, 1).color(0, 0, 0, 255).endVertex();

				BufferUploader.drawWithShader(buffer.end());

				pPoseStack.popPose();
			}
		}

		RenderSystem.enableCull();
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);

		pPoseStack.popPose();
	}

	@SubscribeEvent
	public static void onDrawBlockHighlightEvent(RenderHighlightEvent.Block event){
		// Hide the block outline for magic light blocks unless the player can dispel them
		if(event.getTarget().getType() == HitResult.Type.BLOCK
				&& event.getCamera().getEntity().level.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockMagicLight){

			if(!(event.getCamera().getEntity() instanceof Player))
				return;
			Player player = (Player) event.getCamera().getEntity();
			if((!(player.getMainHandItem().getItem() instanceof ISpellCastingItem)
					&& !(player.getOffhandItem().getItem() instanceof ISpellCastingItem))
					|| !ItemArtefact.isArtefactActive(player, WizardryItems.CHARM_LIGHT.get())){

				event.setCanceled(true);
			}
		}
	}

}
