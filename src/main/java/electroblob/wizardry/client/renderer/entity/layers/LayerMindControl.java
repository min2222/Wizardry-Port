package electroblob.wizardry.client.renderer.entity.layers;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Possession;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Layer used to render the mind control overlay on creatures with the mind control effect.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class LayerMindControl extends LayerTiledOverlay<LivingEntity> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/mind_control_overlay.png");

	public LayerMindControl(RenderLivingBase<?> renderer){
		super(renderer);
	}

	@Override
	public boolean shouldRender(LivingEntity entity, float partialTicks){
		return !entity.isInvisible() && (entity.isPotionActive(WizardryPotions.mind_control) || entity.getEntityData().getBoolean(Possession.NBT_KEY));
	}

	@Override
	public ResourceLocation getTexture(LivingEntity entity, float partialTicks){
		return TEXTURE;
	}

	@Override
	public void doRenderLayer(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale){

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);

		MobEffectInstance effect = entity.getActivePotionEffect(WizardryPotions.mind_control);
		if(effect != null){
			GlStateManager.color(1, 1, 1, Math.min(1, (effect.getDuration() - partialTicks) / 20));
		}

		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
	}

	@Override
	protected void applyTextureSpaceTransformations(LivingEntity entity, float partialTicks){
		float f = entity.tickCount + partialTicks;
		GlStateManager.translate(f * 0.003f, f * 0.003f, 0);
	}
}