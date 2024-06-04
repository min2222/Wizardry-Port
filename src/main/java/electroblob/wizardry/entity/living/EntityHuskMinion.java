package electroblob.wizardry.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;

public class EntityHuskMinion extends EntityZombieMinion {

	/** Creates a new husk minion in the given world. */
	public EntityHuskMinion(Level world){
		super(world);
	}

	@Override
	protected boolean shouldBurnInDay(){
		return false;
	}

	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.ENTITY_HUSK_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource damageSourceIn){ return SoundEvents.ENTITY_HUSK_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.ENTITY_HUSK_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.ENTITY_HUSK_STEP; }

	@Override
	public boolean attackEntityAsMob(Entity target){

		boolean flag = super.attackEntityAsMob(target);

		if(flag && this.getHeldItemMainhand().isEmpty() && target instanceof LivingEntity){
			float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
			((LivingEntity)target).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)f));
		}

		return flag;
	}
}