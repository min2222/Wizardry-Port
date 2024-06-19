package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.entity.living.EntityBlazeMinion;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.resources.ResourceLocation;

//@SideOnly(Side.CLIENT)
public class RenderWraithMinion extends RenderLiving<EntityBlazeMinion> {
	private ResourceLocation texture = new ResourceLocation("textures/entity/blaze.png");

	public RenderWraithMinion(EntityRendererProvider.Context renderManagerIn){
		super(renderManagerIn, new ModelBlaze(), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityBlazeMinion entity){
		return texture;
	}
}
