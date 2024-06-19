package electroblob.wizardry.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntitySpectralGolem;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.EntityIronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSpectralGolem extends MobRenderer<EntitySpectralGolem, IronGolemModel<EntitySpectralGolem>>
{
    private static final ResourceLocation SPECTRAL_GOLEM_TEXTURES = new ResourceLocation(Wizardry.MODID, "textures/entity/spectral_golem.png");

    public RenderSpectralGolem(EntityRendererProvider.Context renderManagerIn)
    {
        super(renderManagerIn, new ModelIronGolem(), 0.5F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    public ResourceLocation getTextureLocation(IronGolem entity)
    {
        return SPECTRAL_GOLEM_TEXTURES;
    }

    @Override
    protected void preRenderCallback(EntityIronGolem entity, float partialTickTime){
        super.preRenderCallback(entity, partialTickTime);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1, 1, 1, 0.7f);
    }

    protected void applyRotations(EntityIronGolem entityLiving, float ageInTicks, float rotationYaw, float partialTicks)
    {
        super.applyRotations(entityLiving, ageInTicks, rotationYaw, partialTicks);

        if ((double)entityLiving.limbSwingAmount >= 0.01D)
        {
            float f = 13.0F;
            float f1 = entityLiving.limbSwing - entityLiving.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
            float f2 = (Math.abs(f1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            GlStateManager.rotate(6.5F * f2, 0.0F, 0.0F, 1.0F);
        }
    }
}