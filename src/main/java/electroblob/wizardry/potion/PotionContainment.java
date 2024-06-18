package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber
public class PotionContainment extends PotionMagicEffect {

	public static final String ENTITY_TAG = "containmentPos";

	public PotionContainment(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/containment.png"));
		this.setPotionName("potion." + Wizardry.MODID + ":containment");
	}

	@Override
	public boolean isReady(int duration, int amplifier){
		return true; // Execute the effect every tick
	}

	public static float getContainmentDistance(int effectStrength){
		return 15 - effectStrength * 4;
	}

	@Override
	public void performEffect(LivingEntity target, int strength){
		float maxDistance = getContainmentDistance(strength);

		// Initialise the containment position to the entity's position if it wasn't set already
		if(!target.getPersistentData().contains(ENTITY_TAG)){
			NBTExtras.storeTagSafely(target.getPersistentData(), ENTITY_TAG, NbtUtils.writeBlockPos(new BlockPos(target.position().subtract(0.5, 0.5, 0.5))));
		}

		Vec3 origin = GeometryUtils.getCentre(NbtUtils.readBlockPos(target.getPersistentData().getCompoundTag(ENTITY_TAG)));

		double x = target.getX(), y = target.getY(), z = target.getZ();

		// Containment fields are cubes so we're dealing with each axis separately
		if(target.getBoundingBox().maxX > origin.x + maxDistance) x = origin.x + maxDistance - target.width/2;
		if(target.getBoundingBox().minX < origin.x - maxDistance) x = origin.x - maxDistance + target.width/2;

		if(target.getBoundingBox().maxY > origin.y + maxDistance) y = origin.y + maxDistance - target.getBbHeight();
		if(target.getBoundingBox().minY < origin.y - maxDistance) y = origin.y - maxDistance;

		if(target.getBoundingBox().maxZ > origin.z + maxDistance) z = origin.z + maxDistance - target.width/2;
		if(target.getBoundingBox().minZ < origin.z - maxDistance) z = origin.z - maxDistance + target.width/2;

		if(x != target.getX() || y != target.getY() || z != target.getZ())
		{
			target.addVelocity(0.15 * Math.signum(x - target.getX()), 0.15 * Math.signum(y - target.getY()), 0.15 * Math.signum(z - target.getZ()));
			EntityUtils.undoGravity(target);
			if(target.level.isClientSide){
				target.level.playSound(target.getX(), target.getY(), target.getZ(), WizardrySounds.ENTITY_FORCEFIELD_DEFLECT,
						WizardrySounds.SPELLS, 0.3f, 1f, false);
			}
		}

		// Need to do this here because it's the only way to hook into potion ending both client- and server-side
		if(target.getActivePotionEffect(this).getDuration() <= 1) target.getPersistentData().removeTag(ENTITY_TAG);

	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingTickEvent event){
		// This is LAST-RESORT CLEANUP. It does NOT need checking every tick! We always check for the actual potion anyway.
		if(event.getEntity().tickCount % 20 == 0 && event.getEntity().getPersistentData().contains(ENTITY_TAG)
				&& !event.getEntity().hasEffect(WizardryPotions.containment)){
			event.getEntity().getPersistentData().remove(ENTITY_TAG);
		}
	}

}
