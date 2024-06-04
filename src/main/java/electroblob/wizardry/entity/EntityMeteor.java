package electroblob.wizardry.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Meteor;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityMeteor extends EntityFallingBlock {

	/**
	 * The entity blast multiplier.
	 */
	public float blastMultiplier;
	private boolean damageBlocks;

	public EntityMeteor(Level world){
		super(world);
		// Superconstructor doesn't call this.
		this.setSize(0.98F, 0.98F);
	}

	public EntityMeteor(Level world, double x, double y, double z, float blastMultiplier, boolean damageBlocks){
		super(world, x, y, z, WizardryBlocks.meteor.getDefaultState());
		this.motionY = -1.0D;
		this.setFire(200);
		this.blastMultiplier = blastMultiplier;
		this.damageBlocks = damageBlocks;
	}

	@Override
	public double getYOffset(){
		return this.getBbHeight() / 2.0F;
	}

	@Override
	public void onUpdate(){

		if(this.tickCount % 16 == 1 && level.isClientSide){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_METEOR_FALLING, WizardrySounds.SPELLS, 3.0f, 1.0f, false);
		}

		// You'd think the best way to do this would be to call super and do all the exploding stuff in fall() instead.
		// However, for some reason, fallTile is null on the client side, causing an NPE in super.onUpdate()

		this.prevgetX() = this.getX();
		this.prevgetY() = this.getY();
		this.prevgetZ() = this.getZ();
		++this.fallTime;
		this.motionY -= 0.1d; // 0.03999999910593033D;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(this.onGround){

			if(!this.level.isClientSide){

				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
				this.motionY *= -0.5D;
				this.world.newExplosion(this, this.getX(), this.getY(), this.getZ(),
						Spells.meteor.getProperty(Meteor.BLAST_STRENGTH).floatValue() * blastMultiplier,
						damageBlocks, damageBlocks);
				this.discard();

			}else{
				EntityUtils.getEntitiesWithinRadius(15, getX(), getY(), getZ(), world, Player.class)
						.forEach(p -> Wizardry.proxy.shakeScreen(p, 10));
			}
		}

	}

	@Override
	public void fall(float distance, float damageMultiplier){
		// Don't need to do anything here, the meteor should have already exploded.
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean canRenderOnFire(){
		return true;
	}

	@Override
	public BlockState getBlock(){
		return WizardryBlocks.meteor.getDefaultState(); // For some reason the superclass version returns null on the
														// client
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getBrightnessForRender(){
		return 15728880;
	}

	@Override
	public float getBrightness(){
		return 1.0F;
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}

	@Override
	public void readEntityFromNBT(CompoundTag nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
		damageBlocks = nbttagcompound.getBoolean("damageBlocks");
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
		nbttagcompound.setBoolean("damageBlocks", damageBlocks);
	}
	
	@Override
	public SoundSource getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

}
