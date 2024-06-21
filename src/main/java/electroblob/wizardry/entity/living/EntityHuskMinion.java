package electroblob.wizardry.entity.living;

import electroblob.wizardry.registry.WizardryEntities;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityHuskMinion extends EntityZombieMinion {

	/** Creates a new husk minion in the given world. */
	public EntityHuskMinion(Level world){
		this(WizardryEntities.HUSK_MINION.get(), world);
	}
	
	public EntityHuskMinion(EntityType<? extends EntityZombieMinion> type, Level world){
		super(type, world);
	}

	@Override
	protected boolean isSunSensitive(){
		return false;
	}

	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.HUSK_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource damageSourceIn){ return SoundEvents.HUSK_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.HUSK_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.HUSK_STEP; }

	@Override
	public boolean doHurtTarget(Entity target){

		boolean flag = super.doHurtTarget(target);

		if(flag && this.getMainHandItem().isEmpty() && target instanceof LivingEntity){
			float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
			((LivingEntity)target).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)f));
		}

		return flag;
	}
}