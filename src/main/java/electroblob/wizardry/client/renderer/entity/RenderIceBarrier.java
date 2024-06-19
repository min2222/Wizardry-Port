package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelIceBarrier;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class RenderIceBarrier extends EntityRenderer<EntityIceBarrier> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/ice_barrier.png");

	private ModelIceBarrier model = new ModelIceBarrier();

	public RenderIceBarrier(Context manager){
		super(manager);
	}

	@Override
	public void doRender(EntityIceBarrier entity, double x, double y, double z, float yaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.getBbHeight()/2, z);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.rotate(yaw, 0, 1, 0);
		GlStateManager.translate(0, -entity.getBbHeight()/2 - 0.3, 0);

		float s = entity.getSizeMultiplier();
		GlStateManager.scale(s, s, s);

		this.bindTexture(TEXTURE);

		model.render(entity, 0, 0, 0, 0, 0, 0.0625f);

		GlStateManager.popMatrix();
	}

	@Override
	public ResourceLocation getTextureLocation(EntityIceBarrier entity){
		return TEXTURE;
	}

}
