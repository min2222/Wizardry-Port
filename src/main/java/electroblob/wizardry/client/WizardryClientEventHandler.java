package electroblob.wizardry.client;

import electroblob.wizardry.client.renderer.overlay.RenderBlinkEffect;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.item.*;
import electroblob.wizardry.potion.PotionSlowTime;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Possession;
import electroblob.wizardry.spell.SixthSense;
import electroblob.wizardry.spell.SlowTime;
import electroblob.wizardry.spell.Transience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * General-purpose client-side event handler for things that don't fit anywhere else or groups of related behaviours
 * that are better kept together.
 *
 * @author Electroblob
 * @since Wizardry 1.0
 */
//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class WizardryClientEventHandler {

	private WizardryClientEventHandler(){} // No instances!

	// Tick Events
	// ===============================================================================================================

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getMinecraft().player && event.phase == TickEvent.Phase.END){

			// Reset shaders if their respective potions aren't active
			// This is a player so the potion effects are synced by vanilla
			if(Minecraft.getMinecraft().entityRenderer.getShaderGroup() != null){ // IntelliJ is wrong, this can be null

				String activeShader = Minecraft.getMinecraft().entityRenderer.getShaderGroup().getShaderGroupName();

				if((activeShader.equals(SlowTime.SHADER.toString()) && !Minecraft.getMinecraft().player.hasEffect(WizardryPotions.slow_time))
						|| (activeShader.equals(SixthSense.SHADER.toString()) && !Minecraft.getMinecraft().player.hasEffect(WizardryPotions.sixth_sense))
						|| (activeShader.equals(Transience.SHADER.toString()) && !Minecraft.getMinecraft().player.hasEffect(WizardryPotions.transience))){

					if(activeShader.equals(SixthSense.SHADER.toString())
					|| activeShader.equals(Transience.SHADER.toString())) RenderBlinkEffect.playBlinkEffect();

					Minecraft.getMinecraft().entityRenderer.stopUseShader();
				}
			}
		}
	}

	// The classes referenced here used to handle their own client tick events, but since they're not client-only
	// classes, that crashed the dedicated server...
	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END && !net.minecraft.client.Minecraft.getMinecraft().isGamePaused()){

			Level world = net.minecraft.client.Minecraft.getMinecraft().world;

			if(world == null) return;

			// Somehow this was throwing a CME, I have no idea why so I'm just going to cheat and copy the list
			List<BlockEntity> tileEntities = new ArrayList<>(world.loadedTileEntityList);

			for(BlockEntity tileentity : tileEntities){
				if(tileentity instanceof DispenserBlockEntity){
					if(DispenserCastingData.get((DispenserBlockEntity)tileentity) != null){
						DispenserCastingData.get((DispenserBlockEntity)tileentity).update();
					}
				}
			}

			SpellEmitterData.update(world);
			PotionSlowTime.cleanUpEntities(world);
		}
	}

	// Input and Controls
	// ===============================================================================================================

	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event){

		// Prevents the player looking around when paralysed
		if(Minecraft.getMinecraft().player.hasEffect(WizardryPotions.paralysis)
				&& Minecraft.getMinecraft().inGameHasFocus){
			event.setCanceled(true);
			Minecraft.getMinecraft().player.prevRotationYaw = 0;
			Minecraft.getMinecraft().player.prevRotationPitch = 0;
			Minecraft.getMinecraft().player.rotationYaw = 0;
			Minecraft.getMinecraft().player.rotationPitch = 0;

		}
	}

	// This event is called every tick, not just when a movement key is pressed
	@SubscribeEvent
	public static void onInputUpdateEvent(InputUpdateEvent event){
		// Prevents the player moving when paralysed
		if(event.getEntity().hasEffect(WizardryPotions.paralysis)){
			event.getMovementInput().moveForward = 0;
			event.getMovementInput().moveStrafe = 0;
			event.getMovementInput().jump = false;
			event.getMovementInput().sneak = false;
		}
		
		if(ItemArtefact.isArtefactActive(event.getEntity(), WizardryItems.charm_move_speed)
				&& event.getEntity().isHandActive()
				&& event.getEntity().getActiveItemStack().getItem() instanceof ISpellCastingItem){
			// Normally speed is set to 20% when using items, this makes it 80%
			event.getMovementInput().moveStrafe *= 4;
			event.getMovementInput().moveForward *= 4;
		}
	}

	// Rendering
	// ===============================================================================================================

	@SubscribeEvent
	public static void onRenderHandEvent(RenderHandEvent event){

		// Hide the player's empty hand in first-person when possessing
		Mob victim = Possession.getPossessee(Minecraft.getMinecraft().player);

		if(victim != null){

			victim.rotationYawHead = Minecraft.getMinecraft().player.rotationYaw;

			if(Minecraft.getMinecraft().player.getMainHandItem().isEmpty()){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onFOVUpdateEvent(FOVUpdateEvent event){

		// Bow zoom. Taken directly from AbstractClientPlayer so it works exactly like vanilla.
		if(event.getEntity().isHandActive()){

			Item item = event.getEntity().getActiveItemStack().getItem();

			if(item instanceof ItemSpectralBow || item instanceof ItemFlamecatcher){

				int maxUseTicks = event.getEntity().getItemInUseMaxCount();

				float maxUseSeconds = (float)maxUseTicks / (item instanceof ItemFlamecatcher ? ItemFlamecatcher.DRAW_TIME : 20);

				if(maxUseSeconds > 1.0F){
					maxUseSeconds = 1.0F;
				}else{
					maxUseSeconds = maxUseSeconds * maxUseSeconds;
				}

				event.setNewfov(event.getFov() * 1.0F - maxUseSeconds * 0.15F);
			}
		}
	}

	/**
	 * Renders an overlay across the entire screen.
	 * @param resolution The screen resolution
	 * @param texture The overlay texture to display
	 */
	public static void renderScreenOverlay(ScaledResolution resolution, ResourceLocation texture){
		
		GlStateManager.pushMatrix();

		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(0, 						resolution.getScaledHeight(), -90).tex(0, 1).endVertex();
		buffer.pos(resolution.getScaledWidth(), resolution.getScaledHeight(), -90).tex(1, 1).endVertex();
		buffer.pos(resolution.getScaledWidth(), 0, 						  -90).tex(1, 0).endVertex();
		buffer.pos(0, 						0, 						  -90).tex(0, 0).endVertex();

		tessellator.draw();
		
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();

		GlStateManager.popMatrix();
	}

}
