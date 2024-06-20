package electroblob.wizardry.potion;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionDecay extends PotionMagicEffect {

	public PotionDecay(MobEffectCategory category, int liquidColour){
		super(category, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/decay.png"));
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				"85602e0b-4801-4a87-94f3-bf617c97014e", -Constants.DECAY_SLOWNESS_PER_LEVEL, Operation.MULTIPLY_TOTAL);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier){
		// Copied from the vanilla wither effect. It does the timing stuff. 25 is the number of ticks between hits at
		// amplifier 0
		int k = 25 >> amplifier;
		return k > 0 ? duration % k == 0 : true;
	}

	@Override
	public void applyEffectTick(LivingEntity host, int strength){
		host.hurt(DamageSource.WITHER, 1);
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){
		
		// This can't be in performEffect because that method is called at a certain frequency which depends on the
		// amplifier of the potion effect, and is too slow for this purpose.
		
		LivingEntity target = event.getEntity();

		// Do the timing check first, it'll cut out 95% of calls to all subsequent conditions
		if(target.tickCount % Constants.DECAY_SPREAD_INTERVAL == 0 && !target.level.isClientSide
				&& target.hasEffect(WizardryPotions.DECAY.get()) && target.isOnGround()){

			List<Entity> entities = target.level.getEntities(target,
					target.getBoundingBox());
			
			for(Entity entity : entities){
				if(entity instanceof EntityDecay) return; // Don't spawn another decay if there's already one there
			}
			
			// The victim spreading the decay is the 'caster' here, so that it can actually wear off, otherwise it
			// just gets infected with its own decay and the effect lasts forever.
			EntityDecay decay = new EntityDecay(target.level);
			decay.setCaster(target);
			decay.setPos(target.getX(), target.getY(), target.getZ());
			target.level.addFreshEntity(decay);
		}
	}

}
