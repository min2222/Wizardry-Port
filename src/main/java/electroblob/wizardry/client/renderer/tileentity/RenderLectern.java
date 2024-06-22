package electroblob.wizardry.client.renderer.tileentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityLectern;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public class RenderLectern implements BlockEntityRenderer<TileEntityLectern> {

	private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/lectern_book.png");
	private final BookModel modelBook;

    public RenderLectern(BlockEntityRendererProvider.Context p_173607_) {
		this.modelBook = new BookModel(p_173607_.bakeLayer(ModelLayers.BOOK));
    }

	@Override
	public void render(TileEntityLectern te, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

		pPoseStack.pushPose();
		pPoseStack.translate(0.5F, 1, 0.5F);
		pPoseStack.mulPose(Vector3f.YP.rotationDegrees(90 - te.getLevel().getBlockState(te.getBlockPos()).getValue(HorizontalDirectionalBlock.FACING).toYRot()));

		float time = (float)te.tickCount + pPartialTick;

		float spread = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * pPartialTick;

		pPoseStack.translate(0, 0.12, 0);
		if(spread > 0.3) pPoseStack.translate(0, Mth.sin(time * 0.1F) * 0.01F, 0);

		pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(112.5F));

		pPoseStack.translate(0, 0.04 + (1 - spread) * 0.09, (1 - spread) * -0.1875);

		pPoseStack.mulPose(Vector3f.ZP.rotationDegrees((1 - spread) * -90));

		float f3 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * pPartialTick + 0.25F;
		float f4 = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * pPartialTick + 0.75F;
		f3 = (f3 - (float) Mth.fastFloor(f3)) * 1.6F - 0.3F;
		f4 = (f4 - (float) Mth.fastFloor(f4)) * 1.6F - 0.3F;

		f3 = Mth.clamp(f3, 0, 1);
		f4 = Mth.clamp(f4, 0, 1);

		RenderSystem.enableCull();
		VertexConsumer vertexconsumer = pBufferSource.getBuffer(this.modelBook.renderType(BOOK_TEXTURE));
		this.modelBook.renderToBuffer(pPoseStack, vertexconsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		this.modelBook.setupAnim(time, f3, f4, spread);
		pPoseStack.popPose();

	}

}
