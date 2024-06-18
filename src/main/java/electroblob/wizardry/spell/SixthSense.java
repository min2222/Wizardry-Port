package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SixthSense extends Spell {

	/** A {@code ResourceLocation} representing the shader file used when under the effects of sixth sense. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/sixth_sense.json");

	public SixthSense(){
		super("sixth_sense", SpellActions.POINT_UP, false);
		addProperties(EFFECT_DURATION, EFFECT_RADIUS);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(Level world, Player caster, InteractionHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.addEffect(new MobEffectInstance(WizardryPotions.sixth_sense,
				(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
				(int)((modifiers.get(WizardryItems.range_upgrade) - 1f) / Constants.RANGE_INCREASE_PER_LEVEL)));

		if(world.isClientSide){
			Wizardry.proxy.loadShader(caster, SHADER);
			Wizardry.proxy.playBlinkEffect(caster);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@SubscribeEvent
	public static void onPotionAddedEvent(MobEffectEvent.Added event){
		if(event.getEntity().level.isClientSide && event.getEffectInstance().getEffect() == WizardryPotions.sixth_sense
				&& event.getEntity() instanceof Player){
			Wizardry.proxy.loadShader((Player)event.getEntity(), SHADER);
			Wizardry.proxy.playBlinkEffect((Player)event.getEntity());
		}
	}

}
