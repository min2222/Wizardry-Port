package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionFrost extends PotionMagicEffect implements ICustomPotionParticles {

	public PotionFrost(MobEffectCategory category, int liquidColour){
		super(category, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/frost.png"));
		// With -0.5 as the 'amount', frost 1 slows the entity down by a half and frost 2 roots it to the spot
		this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
				"35dded48-2f19-4541-8510-b29e2dc2cd51", -Constants.FROST_SLOWNESS_PER_LEVEL, Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void spawnCustomParticle(Level world, double x, double y, double z){
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(15 + world.random.nextInt(5)).spawn(world);
	}

	@SubscribeEvent
	public static void onBreakSpeedEvent(BreakSpeed event){
		if(event.getEntity().hasEffect(WizardryPotions.FROST.get())){
			// Amplifier + 1 because it starts at 0
			event.setNewSpeed(event.getOriginalSpeed() * (1 - Constants.FROST_FATIGUE_PER_LEVEL
					* (event.getEntity().getEffect(WizardryPotions.FROST.get()).getAmplifier() + 1)));
		}
	}

	@SubscribeEvent
	public static void onLivingJumpEvent(LivingJumpEvent event){
		if(event.getEntity().hasEffect(WizardryPotions.FROST.get())){
			if(event.getEntity().getEffect(WizardryPotions.FROST.get()).getAmplifier() == 0){
				event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(1, 0.5, 1));
			}else{
				event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().x, 0, event.getEntity().getDeltaMovement().z);
			}
		}
	}

	@Override
	public void applyEffectTick(LivingEntity entitylivingbase, int strength) {
		if (entitylivingbase.isOnFire()) {
			if (entitylivingbase.hasEffect(WizardryPotions.FROST.get())) {
				entitylivingbase.removeEffect(WizardryPotions.FROST.get());
				entitylivingbase.clearFire();
			}
		}
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
