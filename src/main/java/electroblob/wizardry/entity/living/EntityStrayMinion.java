package electroblob.wizardry.entity.living;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityStrayMinion extends EntitySkeletonMinion {

	/** Creates a new stray minion in the given world. */
	public EntityStrayMinion(Level world){
		super(world);
	}

	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.STRAY_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource source){ return SoundEvents.STRAY_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.STRAY_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.STRAY_STEP; }

	@Override
	protected AbstractArrow getArrow(ItemStack stack, float distanceFactor){

		AbstractArrow entityarrow = super.getArrow(stack, distanceFactor);

		if(entityarrow instanceof Arrow){
			((Arrow)entityarrow).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600));
		}

		return entityarrow;
	}
}
