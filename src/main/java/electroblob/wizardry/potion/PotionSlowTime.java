package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.SlowTime;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.IProjectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class PotionSlowTime extends PotionMagicEffect implements ISyncedPotion {

	// FIXME: Minecarts with entities in them (and, I suspect, any other ridden entities) go crazy when time-slowed

	public static final String NBT_KEY = "time_slowed";

	public PotionSlowTime(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/slow_time.png"));
		this.setPotionName("potion." + Wizardry.MODID + ":slow_time");
	}

	private static double getEffectRadius(){
		return Spells.slow_time.getProperty(Spell.EFFECT_RADIUS).doubleValue();
	}

	public static void unblockNearbyEntities(LivingEntity host){
		List<Entity> targetsBeyondRange = EntityUtils.getEntitiesWithinRadius(getEffectRadius() + 3, host.getX(), host.getY(), host.getZ(), host.world, Entity.class);
		targetsBeyondRange.forEach(e -> e.updateBlocked = false);
	}

	// Not done in performEffect because it's client-inconsistent; it only fires on the client of the player with the
	// potion effect, and doesn't fire on the client at all for non-players
	private static void performEffectConsistent(LivingEntity host, int strength){

		boolean stopTime = host instanceof Player && ItemArtefact.isArtefactActive((Player)host, WizardryItems.charm_stop_time);

		int interval = strength * 4 + 6;

		// Mark all entities within range
		List<Entity> targetsInRange = EntityUtils.getEntitiesWithinRadius(getEffectRadius(), host.getX(), host.getY(), host.getZ(), host.world, Entity.class);
		targetsInRange.remove(host);
		// Other entities with the slow time effect are unaffected
		targetsInRange.removeIf(t -> t instanceof LivingEntity && ((LivingEntity)t).isPotionActive(WizardryPotions.slow_time));
		if(!Wizardry.settings.slowTimeAffectsPlayers) targetsInRange.removeIf(t -> t instanceof Player);
		targetsInRange.removeIf(t -> t instanceof Arrow && t.isEntityInsideOpaqueBlock());

		for(Entity entity : targetsInRange){

			entity.getEntityData().setBoolean(NBT_KEY, true);

			// If time is stopped, block all updates; otherwise block all updates except every [interval] ticks
			entity.updateBlocked = stopTime || host.ticksExisted % interval != 0;

			if(!stopTime && entity.level.isClientSide){

				// Client-side movement interpolation (smoothing)

				if(entity.onGround) entity.motionY = 0; // Don't ask. It just works.

//				if(entity instanceof EntityLivingBase){
//					((EntityLivingBase)entity).prevLimbSwingAmount = ((EntityLivingBase)entity).limbSwingAmount;
//					((EntityLivingBase)entity).swingProgress = ((EntityLivingBase)entity).prevSwingProgress;
//					((EntityLivingBase)entity).renderYawOffset = ((EntityLivingBase)entity).prevRenderYawOffset;
//					((EntityLivingBase)entity).rotationYawHead = ((EntityLivingBase)entity).prevRotationYawHead;
//				}

				if(entity.updateBlocked){
					// When the update is blocked, the entity is moved 1/interval times the distance it would have moved
					double x = entity.getX() + entity.motionX * 1d / (double)interval;
					double y = entity.getY() + entity.motionY * 1d / (double)interval;
					double z = entity.getZ() + entity.motionZ * 1d / (double)interval;

					entity.prevgetX() = entity.getX();
					entity.prevgetY() = entity.getY();
					entity.prevgetZ() = entity.getZ();

					entity.getX() = x;
					entity.getY() = y;
					entity.getZ() = z;

				}else{
					// When the update is not blocked, the entity is moved BACK 1-1/interval times the distance it moved
					// This is because the entity already covered most of that distance when its update was blocked
					entity.getX() += entity.motionX * 1d / (double)interval;
					entity.getY() += entity.motionY * 1d / (double)interval;
					entity.getZ() += entity.motionZ * 1d / (double)interval;

					double x = entity.getX() - entity.motionX * 1d / (double)interval;
					double y = entity.getY() - entity.motionY * 1d / (double)interval;
					double z = entity.getZ() - entity.motionZ * 1d / (double)interval;

					entity.prevgetX() = x;
					entity.prevgetY() = y;
					entity.prevgetZ() = z;
				}
			}

			if(entity.level.isClientSide && host.ticksExisted % 2 == 0){
				int lifetime = 15;
				double dx = (entity.world.random.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double dy = (entity.world.random.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double dz = (entity.world.random.nextDouble() - 0.5D) * 2 * (double)entity.width;
				double x = entity.getX() + dx;
				double y = entity instanceof IProjectile ? entity.getY() + dy : entity.getY() + entity.getBbHeight()/2 + dy;
				double z = entity.getZ() + dz;
				ParticleBuilder.create(ParticleBuilder.Type.DUST)
						.pos(x, y, z)
						.vel(-dx/lifetime, -dy/lifetime, -dz/lifetime)
						.clr(0x5be3bb).time(15).spawn(entity.world);
			}
		}

		// Un-mark all entities that have just left range
		List<Entity> targetsBeyondRange = EntityUtils.getEntitiesWithinRadius(getEffectRadius() + 3, host.getX(), host.getY(), host.getZ(), host.world, Entity.class);
		targetsBeyondRange.removeAll(targetsInRange);
		targetsBeyondRange.forEach(e -> e.updateBlocked = false);

	}

	/**
	 * Goes through every entity in the given world and does the following:<br>
	 * 1. Checks if they have the slow time NBT tag<br>
	 * 2. If so, scans the area nearby for players or NPCs with the slow time effect<br>
	 * 3. If none are found, removes the slow time NBT tag and unblocks the entity's updates
	 */
	public static void cleanUpEntities(Level world){
		// Had trouble with accessing loadedTileEntityList from tick events causing random CMEs so I'm making a
		// copy of this too just in case
		List<Entity> loadedEntityList = new ArrayList<>(world.loadedEntityList);

		for(Entity entity : loadedEntityList){
			if(entity.getEntityData().getBoolean(NBT_KEY)){
				// Currently only players can cast slow time, but you could apply the effect to NPCs with commands
				List<LivingEntity> nearby = EntityUtils.getLivingWithinRadius(getEffectRadius(), entity.getX(), entity.getY(), entity.getZ(), entity.world);
				if(nearby.stream().noneMatch(e -> e.isPotionActive(WizardryPotions.slow_time))){
					entity.getEntityData().removeTag(NBT_KEY);
					entity.updateBlocked = false;
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){

		LivingEntity entity = event.getEntityLiving();

		if(entity.isPotionActive(WizardryPotions.slow_time)){
			performEffectConsistent(entity, entity.getActivePotionEffect(WizardryPotions.slow_time).getAmplifier());
		}
	}

	@SubscribeEvent
	public static void onPotionAddedEvent(PotionEvent.PotionAddedEvent event){
		if(event.getEntity().level.isClientSide && event.getPotionEffect().getPotion() == WizardryPotions.slow_time
				&& event.getEntity() instanceof Player){
			Wizardry.proxy.loadShader((Player)event.getEntity(), SlowTime.SHADER);
			Wizardry.proxy.playBlinkEffect((Player)event.getEntity());
		}
	}

	@SubscribeEvent
	public static void tick(TickEvent.WorldTickEvent event){
		if(!event.level.isClientSide && event.phase == TickEvent.Phase.END) cleanUpEntities(event.world);
	}

	// We still need this as well as tick events because the player hasn't moved anywhere, they just logged out
	// In fact, it won't really matter since the tick event fixes it on login anyway, but if the mod is uninstalled or
	// something else weird happens...
	@SubscribeEvent
	public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
		if(event.player.updateBlocked) event.player.updateBlocked = false;
	}

}
