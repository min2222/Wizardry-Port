package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ParticleBlockHighlight extends ParticleWizardry {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "particle/block_highlight");

	public ParticleBlockHighlight(Level world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURE);
		
		this.particleGravity = 0;
		this.setMaxAge(160);
		// 5 makes it the exact size of a block face, plus a little bit to account for the anti-z-fighting offset
		this.particleScale = 5 * (1 + 2 * (float)GeometryUtils.ANTI_Z_FIGHTING_OFFSET);
		this.shaded = false;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		if(this.particleAge > this.particleMaxAge/2){
			this.setAlphaF(1 - ((float)this.particleAge - this.particleMaxAge/2f) / (this.particleMaxAge/2f));
		}
		
		Direction facing = Direction.fromAngle(yaw);
		if(pitch == 90) facing = Direction.UP;
		if(pitch == -90) facing = Direction.DOWN;
		
		// Disappears if there is no block behind it (this is the same check used to spawn it)
		if(!world.getBlockState(new BlockPos(posX, posY, posZ).offset(facing.getOpposite())).getMaterial().isSolid()){
			this.setExpired();
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(TEXTURE);
	}
}
