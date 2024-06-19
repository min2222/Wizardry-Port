package electroblob.wizardry.potion;

import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
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
	public static void onPotionAddedEvent(MobEffectEvent.Added event){

		if(event.getEffectInstance().getEffect() instanceof ISyncedPotion
		&& ((ISyncedPotion)event.getEffectInstance().getEffect()).shouldSync(event.getEntity())){

			if(!event.getEntity().level.isClientSide){
				event.getEntity().level.players().stream()
						.filter(p -> p.distanceToSqr(event.getEntity()) < SYNC_RADIUS * SYNC_RADIUS)
						// Apparently unchecked casting in a lambda expression doesn't generate a warning. Who knew?
						// (We know this cast is safe though)
						.forEach(p -> ((ServerPlayer)p).connection.send(new ClientboundUpdateMobEffectPacket(
								event.getEntity().getId(), event.getEffectInstance())));
			}
		}
	}

	@SubscribeEvent
	public static void onPotionExpiryEvent(MobEffectEvent.Expired event){
		onPotionEffectEnd(event.getEffectInstance(), event.getEntity());
	}

	@SubscribeEvent
	public static void onPotionRemoveEvent(MobEffectEvent.Remove event){
		onPotionEffectEnd(event.getEffectInstance(), event.getEntity());
	}

	static void onPotionEffectEnd(MobEffectInstance effect, LivingEntity host){

		if(effect != null && effect.getEffect() instanceof ISyncedPotion
				&& ((ISyncedPotion)effect.getEffect()).shouldSync(host)){

			if(!host.level.isClientSide){
				host.level.players().stream()
						.filter(p -> p.distanceToSqr(host) < SYNC_RADIUS * SYNC_RADIUS)
						.forEach(p -> ((ServerPlayer)p).connection.send(new ClientboundRemoveMobEffectPacket(
								host.getId(), effect.getEffect())));
			}
		}
	}

}
