package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EntityLightningDisc extends EntityMagicProjectile {
	
	public EntityLightningDisc(Level world){
		super(world);
		this.width = 2.0f;
		this.getBbHeight() = 0.5f;
	}

	@Override
	protected void onHit(HitResult result){
		
		Entity entityHit = result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) result).getEntity() : null;

		if(entityHit != null){
			float damage = Spells.lightning_disc.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
			entityHit.hurt(MagicDamage.causeIndirectMagicDamage(this, this.getOwner(),
					DamageType.SHOCK), damage);
		}

		this.playSound(WizardrySounds.ENTITY_LIGHTNING_DISC_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

		if(result.getType() == HitResult.Type.BLOCK) this.discard();
	}

	@Override
	public void tick(){

		super.tick();

		// Particle effect
		if(level.isClientSide){
			for(int i = 0; i < 8; i++){
				ParticleBuilder.create(Type.SPARK).pos(this.getX() + random.nextFloat() * 2 - 1,
						this.getY(), this.getZ() + random.nextFloat() * 2 - 1).spawn(level);
			}
		}

		// Cancels out the slowdown effect in EntityThrowable
		this.setDeltaMovement(this.getDeltaMovement().x / 0.99, this.getDeltaMovement().y / 0.99, this.getDeltaMovement().z / 0.99);
	}

	@Override
	public float getSeekingStrength(){
		return Spells.lightning_disc.getProperty(Spell.SEEKING_STRENGTH).floatValue();
	}

	@Override
	public int getLifetime(){
		return 30;
	}

	@Override
	public boolean isNoGravity(){
		return true;
	}

	@Override
	public boolean displayFireAnimation(){
		return false;
	}
}
