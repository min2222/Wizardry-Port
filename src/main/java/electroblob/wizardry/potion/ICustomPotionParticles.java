package electroblob.wizardry.potion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

/**
 * Interface for potion effects that spawn custom particles instead of (or as well as) the vanilla 'swirly' particles.<br>
 * <br>
 * To hide the vanilla 'swirly' particles, set the potion's liquid colour to 0 (black). By default, potions that
 * implement this interface do not mix their colour with other potions.<br>
 * <br>
 * Potions that implement this interface also implement {@link ISyncedPotion} since any custom particles require syncing
 * to disappear correctly when the effect ends; if syncing is not required, override
 * {@link ISyncedPotion#shouldSync(LivingEntity)} to return false.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
@Mod.EventBusSubscriber
public interface ICustomPotionParticles extends ISyncedPotion {

	/**
	 * Called from the event handler to spawn a <b>single</b> custom potion particle. To get an instance of
	 * <code>Random</code> inside this method, use <code>world.rand</code>.
	 * 
	 * @param world The world to spawn the particle in.
	 * @param x The x coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param y The y coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param z The z coordinate of the particle, already set to a random value within the entity's bounding box.
	 */
	void spawnCustomParticle(Level world, double x, double y, double z);
	
	/** Returns true if this potion should mix its colour with others, false if not. Defaults to false. */
	default boolean shouldMixColour(){
		return false;
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingTickEvent event){
		if(event.getEntity().level.isClientSide){
			// Behold the power of interfaces!
			for(MobEffectInstance effect : event.getEntity().getActiveEffects()){

				if(effect.getEffect() instanceof ICustomPotionParticles && effect.isVisible()){

					double x = event.getEntity().getX()
							+ (event.getEntity().level.random.nextDouble() - 0.5) * event.getEntity().getBbWidth();
					double y = event.getEntity().getY()
							+ event.getEntity().level.random.nextDouble() * event.getEntity().getBbHeight();
					double z = event.getEntity().getZ()
							+ (event.getEntity().level.random.nextDouble() - 0.5) * event.getEntity().getBbWidth();

					((ICustomPotionParticles)effect.getEffect()).spawnCustomParticle(event.getEntity().level, x, y, z);
				}
			}
		}
	}
	
	@SubscribeEvent
	// Prevents instances of this interface for which shouldMixColour() returns false from affecting mixed potion colours
	public static void onPotionColourCalculationEvent(PotionColorCalculationEvent event){
		event.setColor(PotionUtils.getColor(event.getEffects().stream().filter(
				p -> !(p instanceof ICustomPotionParticles && !((ICustomPotionParticles)p).shouldMixColour()))
				.collect(Collectors.toList())));
	}

}
