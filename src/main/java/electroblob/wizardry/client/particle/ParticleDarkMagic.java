package electroblob.wizardry.client.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

//@SideOnly(Side.CLIENT)
public class ParticleDarkMagic extends ParticleWizardry {

	/** Base spell texture index */
	private int baseSpellTextureIndex = 128;

	public ParticleDarkMagic(Level world, double x, double y, double z){
		super(world, x, y, z);
		
		this.motionY *= 0.20000000298023224D;
		this.setRBGColorF(1, 1, 1);
		this.particleScale *= 0.75F;
		this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
		this.canCollide = true;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partialTicks, float rotationX, float rotationZ,
                               float rotationYZ, float rotationXY, float rotationXZ){
		float f6 = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;

		if(f6 < 0.0F){
			f6 = 0.0F;
		}

		if(f6 > 1.0F){
			f6 = 1.0F;
		}

		super.renderParticle(buffer, entity, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public void tick(){
		this.prevgetX() = this.getX();
		this.prevgetY() = this.getY();
		this.prevgetZ() = this.getZ();

		if(this.particleAge++ >= this.particleMaxAge){
			this.setExpired();
		}

		this.setParticleTextureIndex(this.baseSpellTextureIndex + (7 - this.particleAge * 8 / this.particleMaxAge));
		this.motionY += 0.004D;
		this.move(this.motionX, this.motionY, this.motionZ);
		/* if (this.getY() == this.prevgetY()) { this.motionX *= 1.1D; this.motionZ *= 1.1D; } */
		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if(this.onGround){
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

	/**
	 * Sets the base spell texture index
	 */
	public void setBaseSpellTextureIndex(int par1){
		this.baseSpellTextureIndex = par1;
	}
}
