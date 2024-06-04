package electroblob.wizardry.entity;

import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Shield;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.IProjectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;

public class EntityShield extends Entity {

	public WeakReference<Player> player;

	public EntityShield(Level world){
		super(world);
		this.noClip = true;
		this.width = 1.2f;
		this.getBbHeight() = 1.4f;
	}

	public EntityShield(Level par1World, Player player){
		super(par1World);
		this.width = 1.2f;
		this.getBbHeight() = 1.4f;
		this.player = new WeakReference<Player>(player);
		this.noClip = true;
		this.setPositionAndRotation(player.getX() + player.getLookVec().x,
				player.getY() + 1 + player.getLookVec().y, player.getZ() + player.getLookVec().z,
				player.rotationYawHead, player.rotationPitch);
		this.setEntityBoundingBox(new AABB(this.getX() - 0.6f, this.getY() - 0.7f, this.getZ() - 0.6f,
				this.getX() + 0.6f, this.getY() + 0.7f, this.getZ() + 0.6f));
	}

	@Override
	public void tick(){
		// System.out.println("Shield exists, ID: " + this.getUUID().toString());
		Player entityplayer = player != null ? player.get() : null;
		if(entityplayer != null){
			this.setPositionAndRotation(entityplayer.getX() + entityplayer.getLookVec().x * 0.3,
					entityplayer.getY() + 1 + entityplayer.getLookVec().y * 0.3,
					entityplayer.getZ() + entityplayer.getLookVec().z * 0.3, entityplayer.rotationYawHead,
					entityplayer.rotationPitch);
			if(!entityplayer.isHandActive() || !(entityplayer.getItemInHand(entityplayer.getActiveHand()).getItem() instanceof ISpellCastingItem)){
				WizardData.get(entityplayer).setVariable(Shield.SHIELD_KEY, null);
				this.discard();
			}
		}else if(!level.isClientSide){
			this.discard();
		}
	}

	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
	// it to stick in blocks.
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9){
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	public boolean hurt(DamageSource source, float damage){
		if(source != null && source.getDirectEntity() instanceof IProjectile){
			world.playSound(null, source.getDirectEntity().getX(), source.getDirectEntity().getY(),
					source.getDirectEntity().getZ(), WizardrySounds.ENTITY_SHIELD_DEFLECT, WizardrySounds.SPELLS, 0.3f, 1.3f);
		}
		super.hurt(source, damage);
		return false;
	}
	
	@Override
	public SoundSource getSoundCategory(){
		return WizardrySounds.SPELLS;
	}

	public boolean canBeCollidedWith(){
		return !this.isDead;
	}

	public AABB getCollisionBox(Entity par1Entity){
		return par1Entity.getBoundingBox();
	}

	@Override
	protected void entityInit(){

	}

	@Override
	protected void readEntityFromNBT(CompoundTag nbttagcompound){

	}

	@Override
	protected void writeEntityToNBT(CompoundTag nbttagcompound){

	}

}
