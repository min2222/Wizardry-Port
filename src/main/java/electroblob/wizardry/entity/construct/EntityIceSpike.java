package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class EntityIceSpike extends EntityMagicConstruct {

	private Direction facing;

	public EntityIceSpike(Level world){
		super(world);
		this.setSize(0.5f, 1.0f);
	}

	public void setFacing(Direction facing){
		this.facing = facing;
		this.setRotation(-facing.getHorizontalAngle(), GeometryUtils.getPitch(facing));
		float yaw = (-facing.getHorizontalAngle()) * (float)Math.PI/180;
		float pitch = (GeometryUtils.getPitch(facing) - 90) * (float)Math.PI/180;
		Vec3 min = this.position().add(new Vec3(-width/2, 0, -width/2).rotatePitch(pitch).rotateYaw(yaw));
		Vec3 max = this.position().add(new Vec3(width/2, height, width/2).rotatePitch(pitch).rotateYaw(yaw));
		this.setEntityBoundingBox(new AABB(min.x, min.y, min.z, max.x, max.y, max.z));
	}

	public Direction getFacing(){
		return facing;
	}

	@Override
	public void tick(){

		double extensionSpeed = 0;

		if(!level.isClientSide){
			if(lifetime - this.tickCount < 15){
				extensionSpeed = -0.01 * (this.tickCount - (lifetime - 15));
			}else if(lifetime - this.tickCount < 25){
				extensionSpeed = 0;
			}else if(lifetime - this.tickCount < 28){
				extensionSpeed = 0.25;
			}

			if(facing != null){ // Will probably be null on the client side, but should never be on the server side
				this.move(MoverType.SELF, this.facing.getXOffset() * extensionSpeed, this.facing.getYOffset() * extensionSpeed,
						this.facing.getZOffset() * extensionSpeed);
			}
		}

		if(lifetime - this.tickCount == 30) this.playSound(WizardrySounds.ENTITY_ICE_SPIKE_EXTEND, 1, 2.5f);

		if(!this.level.isClientSide){
			for(Object entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getBoundingBox())){
				if(entity instanceof LivingEntity && this.isValidTarget((LivingEntity)entity)){
					DamageSource source = this.getCaster() == null ? DamageSource.MAGIC : MagicDamage.causeDirectMagicDamage(this.getCaster(), DamageType.FROST);
					// Potion effect only gets added if the damage succeeded
					// We DO want knockback here or the entity gets stuck on the spike, which is a bit of a cheat
					if(((LivingEntity)entity).hurt(source, Spells.ice_spikes.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier))
						((LivingEntity)entity).addEffect(new MobEffectInstance(WizardryPotions.frost,
								Spells.ice_spikes.getProperty(Spell.EFFECT_DURATION).intValue(),
								Spells.ice_spikes.getProperty(Spell.EFFECT_STRENGTH).intValue()));
				}
			}
		}

		super.tick();
	}

	@Override
	public int getBrightnessForRender(){
		return 15728880;
	}
}
