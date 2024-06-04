package electroblob.wizardry.client.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleCloud extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("cloud", 4);

	public ParticleCloud(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.random.nextInt(TEXTURES.length)]);
		
		this.setRBGColorF(1, 1, 1);
		this.particleMaxAge = 48 + this.random.nextInt(12);
		this.particleScale *= 6;
		this.setGravity(false);
		this.setAlphaF(0);
		this.canCollide = false;
		this.shaded = true;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	@Override
	public void tick(){

		super.tick();
		
		// Fading
		float fadeTime = this.particleMaxAge * 0.3f;
		this.setAlphaF(Mth.clamp(Math.min(this.particleAge / fadeTime, (this.particleMaxAge - this.particleAge) / fadeTime), 0, 1));

	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}

}
