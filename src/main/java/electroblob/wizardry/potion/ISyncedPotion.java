package electroblob.wizardry.potion;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Interface for potion effects that need syncing to ensure client and server side are consistent. Simply implement
 * this interface and the potion will be synced automatically.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
@Mod.EventBusSubscriber
public interface ISyncedPotion {

	/** The distance from an entity with this effect within which players will receive potion update packets. */
	double SYNC_RADIUS = 64;

	/** Returns true if this potion should sync with nearby clients when added to / removed from an entity and on
	 * expiry, false if not. The host entity is provided in case syncing is entity-dependent. Defaults to true. */
	default boolean shouldSync(LivingEntity host){
		return true;
	}

	// The following event handlers fix the inconsistencies caused by clients not syncing correctly
	// These packets are only sent for players with potion effects in vanilla, and only to that player's client

	// This one is only actually necessary if the effect gets added via a server-side method e.g. commands
	// Unfortunately there's no way of checking that, so we'll just have to live with the extra packets
	@SubscribeEvent
	public static void onPotionAddedEvent(PotionEvent.PotionAddedEvent event){

		if(event.getPotionEffect().getPotion() instanceof ISyncedPotion
		&& ((ISyncedPotion)event.getPotionEffect().getPotion()).shouldSync(event.getEntity())){

			if(!event.getEntity().level.isClientSide){
				event.getEntity().world.playerEntities.stream()
						.filter(p -> p.distanceToSqr(event.getEntity()) < SYNC_RADIUS * SYNC_RADIUS)
						// Apparently unchecked casting in a lambda expression doesn't generate a warning. Who knew?
						// (We know this cast is safe though)
						.forEach(p -> ((ServerPlayer)p).connection.sendPacket(new SPacketEntityEffect(
								event.getEntity().getEntityId(), event.getPotionEffect())));
			}
		}
	}

	@SubscribeEvent
	public static void onPotionExpiryEvent(PotionEvent.PotionExpiryEvent event){
		onPotionEffectEnd(event.getPotionEffect(), event.getEntity());
	}

	@SubscribeEvent
	public static void onPotionRemoveEvent(PotionEvent.PotionRemoveEvent event){
		onPotionEffectEnd(event.getPotionEffect(), event.getEntity());
	}

	static void onPotionEffectEnd(MobEffectInstance effect, LivingEntity host){

		if(effect != null && effect.getPotion() instanceof ISyncedPotion
				&& ((ISyncedPotion)effect.getPotion()).shouldSync(host)){

			if(!host.level.isClientSide){
				host.world.playerEntities.stream()
						.filter(p -> p.distanceToSqr(host) < SYNC_RADIUS * SYNC_RADIUS)
						.forEach(p -> ((ServerPlayer)p).connection.sendPacket(new SPacketRemoveEntityEffect(
								host.getEntityId(), effect.getPotion())));
			}
		}
	}

}
