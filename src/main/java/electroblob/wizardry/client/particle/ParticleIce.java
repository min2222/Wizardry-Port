package electroblob.wizardry.client.particle;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleIce extends ParticleWizardry {

	private static final ResourceLocation[] TEXTURES = generateTextures("ice", 8);
	
	public ParticleIce(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURES[world.rand.nextInt(TEXTURES.length)]);
		
		this.canCollide = true;
		
		// Defaults
		this.setRBGColorF(1, 1, 1);
		this.particleScale *= 0.75f;
		this.setGravity(true);
		this.shaded = false;
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		for(ResourceLocation texture : TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}
}
