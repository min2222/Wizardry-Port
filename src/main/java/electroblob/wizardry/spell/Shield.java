package electroblob.wizardry.spell;

import electroblob.wizardry.data.IVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

public class Shield extends Spell {

	public static final IVariable<EntityShield> SHIELD_KEY = new IVariable.Variable<>(Persistence.NEVER);

	public Shield(){
		super("shield", UseAnim.BLOCK, true);
		addProperties(EFFECT_STRENGTH);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(Level world, LivingEntity entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(Level world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10,
				getProperty(EFFECT_STRENGTH).intValue(), false, false));

		if(WizardData.get(caster).getVariable(SHIELD_KEY) == null){

			EntityShield shield = new EntityShield(world, caster);

			WizardData.get(caster).setVariable(SHIELD_KEY, shield);
			if(!level.isClientSide){
				world.spawnEntity(shield);
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
