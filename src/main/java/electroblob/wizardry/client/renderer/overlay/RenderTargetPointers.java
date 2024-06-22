package electroblob.wizardry.client.renderer.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.RayTracer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class RenderTargetPointers {

	private static final ResourceLocation POINTER_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/pointer.png");
	private static final ResourceLocation TARGET_POINTER_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/target_pointer.png");

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event){

		Minecraft mc = Minecraft.getInstance();
        PoseStack stack = event.getPoseStack();
		WizardData data = WizardData.get(mc.player);

		ItemStack wand = mc.player.getMainHandItem();

		if(!(wand.getItem() instanceof ISpellCastingItem)){
			wand = mc.player.getOffhandItem();
		}

		// Target selection pointer
		if(mc.player.isShiftKeyDown() && wand.getItem() instanceof ISpellCastingItem && EntityUtils.isLiving(event.getEntity())
				&& data != null && data.selectedMinion != null){

			// -> Moved this in here so it isn't called every tick
			EntityHitResult rayTrace = (EntityHitResult) RayTracer.standardEntityRayTrace(mc.level, mc.player, 16, false);

			if(rayTrace != null && rayTrace.getEntity() == event.getEntity()){

				Tesselator tessellator = Tesselator.getInstance();
				BufferBuilder buffer = tessellator.getBuilder();

				stack.pushPose();

				RenderSystem.disableCull();
				// Disabling depth test allows it to be seen through everything.
				RenderSystem.disableDepthTest();
				RenderSystem.setShaderColor(1, 1, 1, 1);

				stack.translate(0, event.getEntity().getBbHeight() + 0.5, 0);

				// This counteracts the reverse rotation behaviour when in front f5 view.
				// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
	            stack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
	            stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
	            
	            RenderSystem.setShader(GameRenderer::getPositionTexShader);
	            RenderSystem.setShaderTexture(0, TARGET_POINTER_TEXTURE);

	            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

				buffer.vertex(stack.last().pose(), -0.2f, 0.24f, 0).uv(0, 0).endVertex();
				buffer.vertex(stack.last().pose(), 0.2f, 0.24f, 0).uv(9f / 16f, 0).endVertex();
				buffer.vertex(stack.last().pose(), 0.2f, -0.24f, 0).uv(9f / 16f, 11f / 16f).endVertex();
				buffer.vertex(stack.last().pose(), -0.2f, -0.24f, 0).uv(0, 11f / 16f).endVertex();

	            BufferUploader.drawWithShader(buffer.end());

	            RenderSystem.enableCull();
	            RenderSystem.enableDepthTest();

				stack.popPose();
			}
		}

		// Summoned creature selection pointer
		if(data != null && data.selectedMinion != null && data.selectedMinion.get() == event.getEntity()){

			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder buffer = tessellator.getBuilder();

			stack.pushPose();

			RenderSystem.disableCull();
			// Disabling depth test allows it to be seen through everything.
			RenderSystem.disableDepthTest();
			RenderSystem.setShaderColor(1, 1, 1, 1);

			stack.translate(0, event.getEntity().getBbHeight() + 0.5, 0);

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
            stack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, POINTER_TEXTURE);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            buffer.vertex(stack.last().pose(), -0.2f, 0.24f, 0).uv(0, 0).endVertex();
			buffer.vertex(stack.last().pose(), 0.2f, 0.24f, 0).uv(9f / 16f, 0).endVertex();
			buffer.vertex(stack.last().pose(), 0.2f, -0.24f, 0).uv(9f / 16f, 11f / 16f).endVertex();
			buffer.vertex(stack.last().pose(), -0.2f, -0.24f, 0).uv(0, 11f / 16f).endVertex();

            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();

			stack.popPose();
		}
	}

}
