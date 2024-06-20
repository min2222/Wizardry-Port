package electroblob.wizardry.potion;

import java.lang.reflect.Field;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber
public class CurseEnfeeblement extends Curse {

	// Yay more reflection
	private static final Field foodTimer;

	static {
		foodTimer = ObfuscationReflectionHelper.findField(FoodData.class, "field_75123_d");
		foodTimer.setAccessible(true);
	}

	public CurseEnfeeblement(MobEffectCategory category, int liquiidColour){
		super(category, liquiidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_enfeeblement.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.addAttributeModifier(Attributes.MAX_HEALTH,
				"2e8c378e-3d51-4ba1-b02c-591b5d968a05", -0.2, Operation.MULTIPLY_BASE);
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){
		// Players are the only entities with natural regeneration
		// This can't be done in performEffect as that method only gets called every 20 ticks or so
		// Don't bother trying to prevent it unless the player is full enough
		if(event.player.hasEffect(WizardryPotions.CURSE_OF_ENFEEBLEMENT.get()) && event.player.getFoodData().getFoodLevel() > 17){
			try{
				// Constantly setting this to zero prevents natural regeneration
				foodTimer.set(event.player.getFoodData(), 0);
			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error setting player food timer: ", e);
			}
		}
	}

}
