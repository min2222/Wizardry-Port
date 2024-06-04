package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.init.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class BlindingFlash extends SpellAreaEffect {

	public BlindingFlash(){
		super("blinding_flash", SpellActions.POINT_UP, false);
		this.alwaysSucceed(true);
		addProperties(EFFECT_DURATION); // No effect strength, you're either blinded or you're not!
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){
			int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
			target.addPotionEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(Level world, Vec3 origin, double radius, @Nullable LivingEntity caster, SpellModifiers modifiers){
		if(caster != null) origin = origin.add(0, caster.height + 1, 0);
		ParticleBuilder.create(Type.SPHERE).pos(origin).scale((float)radius * 0.8f).spawn(world);
	}
}
