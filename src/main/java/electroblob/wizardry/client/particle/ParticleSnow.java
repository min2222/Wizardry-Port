package electroblob.wizardry.client.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleSnow extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("snow", 4);

	public ParticleSnow(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.random.nextInt(TEXTURES.length)]);
		
		this.setVelocity(0, -0.02, 0);
		this.particleScale *= 0.6f;
		this.particleGravity = 0;
		this.canCollide = true;
		this.setMaxAge(40 + random.nextInt(10));
		// Produces a variety of light blues and whites
		this.setRBGColorF(0.9f + 0.1f * random.nextFloat(), 0.95f + 0.05f * random.nextFloat(), 1);
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
