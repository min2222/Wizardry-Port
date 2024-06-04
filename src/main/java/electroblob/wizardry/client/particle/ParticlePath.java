package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Clairvoyance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticlePath extends ParticleWizardry {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "particle/path");

	private final double originX, originY, originZ;

	public ParticlePath(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURE); // This particle only has 1 texture
		
		this.originX = x;
		this.originY = y;
		this.originZ = z;
		
		// Set to a constant to remove the randomness from Particle.
		this.particleScale = 1.25f;
		this.particleGravity = 0;
		this.shaded = false;
		this.canCollide = false;
		this.setRBGColorF(1, 1, 1);
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}

	@Override
	public void tick(){

		this.prevgetX() = this.getX();
		this.prevgetY() = this.getY();
		this.prevgetZ() = this.getZ();

		if(this.particleAge++ >= this.particleMaxAge){
			this.setExpired();
		}

		this.move(this.motionX, this.motionY, this.motionZ);

		// Fading
		if(this.particleAge > this.particleMaxAge / 2){
			this.setAlphaF(1.0F
					- 2 * (((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge));
		}

		if(this.particleAge % Clairvoyance.PARTICLE_MOVEMENT_INTERVAL == 0){
			this.setPosition(this.originX, this.originY, this.originZ);
			this.prevgetX() = this.getX();
			this.prevgetY() = this.getY();
			this.prevgetZ() = this.getZ();
		}

	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(TEXTURE);
	}
	
}
