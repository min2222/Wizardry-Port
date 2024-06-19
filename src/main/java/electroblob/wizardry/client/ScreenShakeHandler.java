package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ScreenShakeHandler {

	/** The remaining time for which the screen shake effect will be active. */
	private static int screenShakeCounter = 0;
	private static final float SHAKINESS = 0.5f;

	/** Starts the client-side screen shake effect. */
	public static void shakeScreen(float intensity){
		if(Wizardry.settings.screenShake){
			screenShakeCounter = (int)(intensity / SHAKINESS);
			Minecraft.getInstance().player.rotationPitch -= intensity * 0.5f; // Start halfway down
		}
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getInstance().player && event.phase == TickEvent.Phase.END){

			if(Wizardry.settings.screenShake){
				if(screenShakeCounter > 0){
					float magnitude = screenShakeCounter * SHAKINESS;
					Minecraft.getInstance().player.rotationPitch += screenShakeCounter % 2 == 0 ? magnitude : -magnitude;
					screenShakeCounter--;
				}
			}else{
				screenShakeCounter = 0;
			}
		}
	}

}
