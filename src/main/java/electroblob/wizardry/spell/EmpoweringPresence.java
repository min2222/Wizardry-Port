package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EmpoweringPresence extends SpellAreaEffect {

	/** The fraction by which potency is increased per level of the empowerment effect. */
	public static final String POTENCY_PER_LEVEL = "potency_per_level";

	public EmpoweringPresence(){
		super("empowering_presence", SpellActions.POINT_UP, false);
		this.alwaysSucceed(true);
		this.targetAllies(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH, POTENCY_PER_LEVEL);
	}

	@Override
	protected boolean affectEntity(Level world, Vec3 origin, @Nullable LivingEntity caster, LivingEntity target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(target instanceof Player || target instanceof ISpellCaster){ // Only useful for spell casters

			int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

			target.addEffect(new MobEffectInstance(WizardryPotions.EMPOWERMENT.get(),
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.DURATION_UPGRADE.get())),
					getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));
		}

		return true;
	}

	@Override
	protected void spawnParticle(Level world, double x, double y, double z){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50).clr(0.5f, 0.4f, 0.75f).spawn(world);
	}

	@Override
	protected String getTranslationKey(){
		return Wizardry.tisTheSeason ? super.getTranslationKey() + "_festive" : super.getTranslationKey();
	}

	@SubscribeEvent(priority = EventPriority.LOW) // Doesn't really matter but there's no point processing it if casting is blocked
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Empowerment stacks extra potency on top of the existing potency.
		if(event.getCaster() != null && event.getCaster().hasEffect(WizardryPotions.EMPOWERMENT.get())
				&& !(event.getSpell() instanceof EmpoweringPresence)){ // Prevent exponential empowerment stacking!

			float potency = 1 + Spells.empowering_presence.getProperty(POTENCY_PER_LEVEL).floatValue()
					* (event.getCaster().getEffect(WizardryPotions.EMPOWERMENT.get()).getAmplifier() + 1);

			event.getModifiers().set(SpellModifiers.POTENCY,
					event.getModifiers().get(SpellModifiers.POTENCY) * potency, true);
		}
	}

}