package electroblob.wizardry.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class RenderBlank extends EntityRenderer<Entity> {

	public RenderBlank(EntityRendererProvider.Context renderManager){
		super(renderManager);
	}
	
	@Override
	public void render(Entity p_114485_, float p_114486_, float p_114487_, PoseStack p_114488_, MultiBufferSource p_114489_, int p_114490_) {
		
	}

	@Override
	public ResourceLocation getTextureLocation(Entity p_114482_) {
		return null;
	}

}
