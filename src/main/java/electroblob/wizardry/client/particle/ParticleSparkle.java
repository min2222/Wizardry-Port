package electroblob.wizardry.client.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleSparkle extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("sparkle", 11);

	public ParticleSparkle(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES); // This time the textures are all one long animation
		
		this.setRBGColorF(1, 1, 1);
		this.particleMaxAge = 48 + this.rand.nextInt(12);
		this.particleScale *= 0.75f;
		this.particleGravity = 0;
		this.canCollide = false;
		this.shaded = false;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(1 - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
