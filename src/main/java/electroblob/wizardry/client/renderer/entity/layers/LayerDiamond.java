package electroblob.wizardry.client.renderer.entity.layers;

import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Layer used to render the diamondflesh texture on creatures with the diamondflesh effect.
 *
 * @author WinDanesz
 * @since Wizardry 4.3.7
 */
public class LayerDiamond extends LayerTiledOverlay<LivingEntity> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/blocks/diamond_block.png");

	public LayerDiamond(RenderLivingBase<?> renderer){
		super(renderer);
	}

	@Override
	public boolean shouldRender(LivingEntity entity, float partialTicks){
		return !entity.isInvisible() && entity.hasEffect(WizardryPotions.diamondflesh);
	}

	@Override
	public ResourceLocation getTexture(LivingEntity entity, float partialTicks){
		return TEXTURE;
	}

	@Override
	public void doRenderLayer(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale){

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		int j = entity.getBrightnessForRender();
        	int k = j % 65536;
			int l = j / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) k, (float) l);

		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
	}

}
