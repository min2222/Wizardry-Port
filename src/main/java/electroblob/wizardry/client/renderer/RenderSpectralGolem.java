package electroblob.wizardry.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntitySpectralGolem;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSpectralGolem extends MobRenderer<EntitySpectralGolem, IronGolemModel<EntitySpectralGolem>>
{
    private static final ResourceLocation SPECTRAL_GOLEM_TEXTURES = new ResourceLocation(Wizardry.MODID, "textures/entity/spectral_golem.png");

    public RenderSpectralGolem(EntityRendererProvider.Context renderManagerIn)
    {
        super(renderManagerIn, new IronGolemModel<>(renderManagerIn.bakeLayer(ModelLayers.IRON_GOLEM)), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    public ResourceLocation getTextureLocation(EntitySpectralGolem entity)
    {
        return SPECTRAL_GOLEM_TEXTURES;
    }

    @Override
    protected void scale(EntitySpectralGolem entity, PoseStack stack, float partialTickTime){
        super.scale(entity, stack, partialTickTime);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1, 1, 1, 0.7f);
    }

    @Override
    protected void setupRotations(EntitySpectralGolem entityLiving, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks)
    {
        super.setupRotations(entityLiving, stack, ageInTicks, rotationYaw, partialTicks);

        if ((double)entityLiving.animationSpeed >= 0.01D)
        {
            float f1 = entityLiving.animationPosition - entityLiving.animationSpeed * (1.0F - partialTicks) + 6.0F;
            float f2 = (Math.abs(f1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            stack.mulPose(Vector3f.ZP.rotationDegrees(6.5F * f2));
        }
    }
}