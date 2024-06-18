package electroblob.wizardry.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Meteor;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityMeteor extends FallingBlockEntity {

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
		super(world, x, y, z, WizardryBlocks.meteor.defaultBlockState());
		this.motionY = -1.0D;
		this.setSecondsOnFire(200);
		this.blastMultiplier = blastMultiplier;
		this.damageBlocks = damageBlocks;
	}

	@Override
	public double getMyRidingOffset(){
		return this.getBbHeight() / 2.0F;
	}

	@Override
	public void tick(){

		if(this.tickCount % 16 == 1 && level.isClientSide){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_METEOR_FALLING, WizardrySounds.SPELLS, 3.0f, 1.0f, false);
		}

		// You'd think the best way to do this would be to call super and do all the exploding stuff in fall() instead.
		// However, for some reason, fallTile is null on the client side, causing an NPE in super.tick()

		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		++this.time;
		this.setDeltaMovement(this.getDeltaMovement().subtract(0, 0.1d, 0)); // 0.03999999910593033D;
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.9800000190734863D));

		if(this.onGround){

			if(!this.level.isClientSide){
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.699999988079071D, -0.5D, 0.699999988079071D));
				this.level.explode(this, this.getX(), this.getY(), this.getZ(),
						Spells.meteor.getProperty(Meteor.BLAST_STRENGTH).floatValue() * blastMultiplier,
						damageBlocks, damageBlocks ? BlockInteraction.DESTROY : BlockInteraction.NONE);
				this.discard();

			}else{
				EntityUtils.getEntitiesWithinRadius(15, getX(), getY(), getZ(), level, Player.class)
						.forEach(p -> Wizardry.proxy.shakeScreen(p, 10));
			}
		}

	}
	
	@Override
	public boolean causeFallDamage(float p_149643_, float p_149644_, DamageSource p_149645_) {
		// Don't need to do anything here, the meteor should have already exploded.
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean displayFireAnimation(){
		return true;
	}

	@Override
	public BlockState getBlockState(){
		return WizardryBlocks.meteor.defaultBlockState(); // For some reason the superclass version returns null on the
														// client
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double distance){
		return true;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbttagcompound){
		super.readAdditionalSaveData(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
		damageBlocks = nbttagcompound.getBoolean("damageBlocks");
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbttagcompound){
		super.addAdditionalSaveData(nbttagcompound);
		nbttagcompound.putFloat("blastMultiplier", blastMultiplier);
		nbttagcompound.putBoolean("damageBlocks", damageBlocks);
	}
	
	@Override
	public SoundSource getSoundSource(){
		return WizardrySounds.SPELLS;
	}

}
