package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class InvigoratingPresence extends SpellAreaEffect {

	public InvigoratingPresence(){
		super("invigorating_presence", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.alwaysSucceed(true);
		this.targetAllies(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

		target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
				(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.DURATION_UPGRADE.get())),
				getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));

		return true;
	}

	@Override
	protected void spawnParticle(Level world, double x, double y, double z){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50).clr(1, 0.2f, 0.2f).spawn(world);
	}

	@Override
	protected String getTranslationKey(){
		return Wizardry.tisTheSeason ? super.getTranslationKey() + "_festive" : super.getTranslationKey();
	}

}
