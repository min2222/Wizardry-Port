package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.spell.Possession;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderPossessingPlayer {

	@SubscribeEvent
	@SuppressWarnings("unchecked") // Can't check it due to type erasure
	public static void onRenderPlayerPreEvent(RenderPlayerEvent.Pre event){

		Player player = event.getEntityPlayer();
		Mob possessee = Possession.getPossessee(player);

		if(possessee != null){
			// I reject your renderer and substitute my own!
			Render<Mob> renderer = (Render<Mob>)event.getRenderer().getRenderManager().entityRenderMap.get(possessee.getClass());
			float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getPartialRenderTick();
			possessee.swingProgress = player.swingProgress;
			possessee.prevSwingProgress = player.prevSwingProgress;
			possessee.renderYawOffset = player.renderYawOffset;
			possessee.prevRenderYawOffset = player.prevRenderYawOffset;
			possessee.rotationYawHead = player.rotationYawHead;
			possessee.prevRotationYawHead = player.prevRotationYawHead;
			possessee.rotationPitch = player.rotationPitch;
			possessee.prevRotationPitch = player.prevRotationPitch;
			possessee.limbSwing = player.limbSwing;
			possessee.limbSwingAmount = player.limbSwingAmount;
			possessee.prevLimbSwingAmount = player.prevLimbSwingAmount;
			renderer.doRender(possessee, event.getX(), event.getY(), event.getZ(), yaw, event.getPartialRenderTick());
			event.setCanceled(true);
		}
	}
}
