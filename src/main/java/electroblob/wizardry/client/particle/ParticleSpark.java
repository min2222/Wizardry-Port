package electroblob.wizardry.client.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleSpark extends ParticleWizardry {

	// 8 different animation strips, 4 in each strip
	private static final ResourceLocation[][] TEXTURES = generateTextures("lightning", 8, 4);

	public ParticleSpark(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.random.nextInt(TEXTURES.length)]);
		
		this.particleScale *= 1.4f;
		this.setRBGColorF(1, 1, 1);
		this.shaded = false;
		this.canCollide = false;
		this.setMaxAge(3); // Lifetime defaults to 3 (and is very unlikely to be changed)
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	// May no longer be necessary, ParticleManager seems to enable blending now

//	@Override
//	public void applyGLStateChanges(){
//		GlStateManager.enableBlend();
//		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//		GlStateManager.disableLighting();
//		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
//	}
//
//	@Override
//	public void undoGLStateChanges(){
//		GlStateManager.disableBlend();
//		GlStateManager.enableLighting();
//	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation[] array : TEXTURES){
			for(ResourceLocation texture : array){
				event.getMap().registerSprite(texture);
			}
		}
	}

}
