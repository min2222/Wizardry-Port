package electroblob.wizardry.client.renderer.tileentity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RenderStatue implements BlockEntityRenderer<TileEntityStatue> {
	
	protected static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[] {
			new ResourceLocation("textures/blocks/destroy_stage_0.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_1.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_2.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_3.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_4.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_5.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_6.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_7.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_8.png"), 
			new ResourceLocation("textures/blocks/destroy_stage_9.png")};
	private int destroyStage = 0; // Gets set each time a statue is rendered to allow access from the layer renderer

	@Override
	public void render(TileEntityStatue statue, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

		// Multiblock support for the breaking animation. The chest has its own way of doing this in
		// TileEntityRendererDispatcher, but I don't have access to that.
		if(statue.position != 1 && destroyStage >= 0){
			BlockEntity tileentity = statue.getLevel().getBlockEntity(statue.getBlockPos().below(statue.position - 1));
			// System.out.println(tileentity);
			if(tileentity instanceof TileEntityStatue){
				// If this is the block breaking animation pass and this isn't the bottom block, divert the call to
				// the bottom block.
				this.render(statue, pPartialTick, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay);
			}
		}

		if(statue.creature != null && statue.position == 1){

			pPoseStack.pushPose();
			// The next line makes stuff render in the same place relative to the world wherever the player is.
			pPoseStack.translate(0.5F, 0, 0.5F);

			float yaw = statue.creature.yRotO;
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
			// Stops the normal model from rendering.
			if(!statue.isIce) statue.creature.setInvisible(true);
			// Setting the last parameter to true prevents the debug bounding box from rendering.
			// For some reason, passing in the partialTicks causes the entity to spin round really fast
			Minecraft.getInstance().getEntityRenderDispatcher().render(statue.creature, 0, 0, 0, 0, 0, pPoseStack, pBufferSource, pPackedLight);
			if(!statue.isIce) statue.creature.setInvisible(false);

			pPoseStack.popPose();

		}
	}

	public ResourceLocation getBlockBreakingTexture(){
		return destroyStage < 0 ? null : DESTROY_STAGES[destroyStage];
	}

}
