package electroblob.wizardry.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import electroblob.wizardry.client.renderer.overlay.RenderBlinkEffect;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemFlamecatcher;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.potion.PotionSlowTime;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Possession;
import electroblob.wizardry.spell.SixthSense;
import electroblob.wizardry.spell.SlowTime;
import electroblob.wizardry.spell.Transience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

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

		if(event.player == Minecraft.getInstance().player && event.phase == TickEvent.Phase.END){

			// Reset shaders if their respective potions aren't active
			// This is a player so the potion effects are synced by vanilla
			if(Minecraft.getInstance().gameRenderer.currentEffect() != null){ // IntelliJ is wrong, this can be null

				String activeShader = Minecraft.getInstance().gameRenderer.currentEffect().getName();

				if((activeShader.equals(SlowTime.SHADER.toString()) && !Minecraft.getInstance().player.hasEffect(WizardryPotions.SLOW_TIME.get()))
						|| (activeShader.equals(SixthSense.SHADER.toString()) && !Minecraft.getInstance().player.hasEffect(WizardryPotions.SIXTH_SENSE.get()))
						|| (activeShader.equals(Transience.SHADER.toString()) && !Minecraft.getInstance().player.hasEffect(WizardryPotions.TRANSIENCE.get()))){

					if(activeShader.equals(SixthSense.SHADER.toString())
					|| activeShader.equals(Transience.SHADER.toString())) RenderBlinkEffect.playBlinkEffect();

					Minecraft.getInstance().gameRenderer.shutdownEffect();
				}
			}
		}
	}

	// The classes referenced here used to handle their own client tick events, but since they're not client-only
	// classes, that crashed the dedicated server...
	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END && !net.minecraft.client.Minecraft.getInstance().isPaused()){

			Level world = net.minecraft.client.Minecraft.getInstance().level;

			if(world == null) return;

			// Somehow this was throwing a CME, I have no idea why so I'm just going to cheat and copy the list
            ArrayList<BlockEntity> freshBlockEntities = ObfuscationReflectionHelper.getPrivateValue(Level.class, world, "freshBlockEntities");
            List<BlockEntity> tileEntities = new ArrayList<>(freshBlockEntities);

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
	public static void onMouseEvent(ViewportEvent.ComputeCameraAngles event){

		// Prevents the player looking around when paralysed
		if(Minecraft.getInstance().player.hasEffect(WizardryPotions.PARALYSIS.get())
				&& Minecraft.getInstance().mouseHandler.isMouseGrabbed()){
			event.setCanceled(true);
            Minecraft.getInstance().player.yRotO = 0;
            Minecraft.getInstance().player.xRotO = 0;
            Minecraft.getInstance().player.setYRot(0);
            Minecraft.getInstance().player.setXRot(0);

		}
	}

	// This event is called every tick, not just when a movement key is pressed
	@SubscribeEvent
	public static void onInputUpdateEvent(MovementInputUpdateEvent event){
		// Prevents the player moving when paralysed
		if(event.getEntity().hasEffect(WizardryPotions.PARALYSIS.get())){
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;
            event.getInput().jumping = false;
            event.getInput().shiftKeyDown = false;
		}
		
		if(ItemArtefact.isArtefactActive(event.getEntity(), WizardryItems.CHARM_MOVE_SPEED.get())
				&& event.getEntity().isUsingItem()
				&& event.getEntity().getUseItem().getItem() instanceof ISpellCastingItem){
			// Normally speed is set to 20% when using items, this makes it 80%
            event.getInput().leftImpulse *= 4;
            event.getInput().forwardImpulse *= 4;
		}
	}

	// Rendering
	// ===============================================================================================================

	@SubscribeEvent
	public static void onRenderHandEvent(RenderHandEvent event){

		// Hide the player's empty hand in first-person when possessing
		Mob victim = Possession.getPossessee(Minecraft.getInstance().player);

		if(victim != null){

            victim.yHeadRot = Minecraft.getInstance().player.getYRot();

			if(Minecraft.getInstance().player.getMainHandItem().isEmpty()){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onFOVUpdateEvent(ViewportEvent.ComputeFov event){

		// Bow zoom. Taken directly from AbstractClientPlayer so it works exactly like vanilla.
		if(event.getCamera().getEntity() == null)
			return;
		if(((LivingEntity) event.getCamera().getEntity()).isUsingItem()){

			Item item = ((LivingEntity) event.getCamera().getEntity()).getUseItem().getItem();

			if(item instanceof ItemSpectralBow || item instanceof ItemFlamecatcher){

				int maxUseTicks = ((LivingEntity) event.getCamera().getEntity()).getTicksUsingItem();

				float maxUseSeconds = (float)maxUseTicks / (item instanceof ItemFlamecatcher ? ItemFlamecatcher.DRAW_TIME : 20);

				if(maxUseSeconds > 1.0F){
					maxUseSeconds = 1.0F;
				}else{
					maxUseSeconds = maxUseSeconds * maxUseSeconds;
				}

				event.setFOV(event.getFOV() * 1.0F - maxUseSeconds * 0.15F);
			}
		}
	}

	/**
	 * Renders an overlay across the entire screen.
	 * @param resolution The screen resolution
	 * @param texture The overlay texture to display
	 */
	public static void renderScreenOverlay(PoseStack stack, Window resolution, ResourceLocation texture){
		
		stack.pushPose();
		
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);

        RenderSystem.setShaderTexture(0, texture);

        Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		buffer.vertex(stack.last().pose(), 0, 						resolution.getGuiScaledHeight(), -90).uv(0, 1).endVertex();
		buffer.vertex(stack.last().pose(), resolution.getGuiScaledWidth(), resolution.getGuiScaledHeight(), -90).uv(1, 1).endVertex();
		buffer.vertex(stack.last().pose(), resolution.getGuiScaledWidth(), 0, 						  -90).uv(1, 0).endVertex();
		buffer.vertex(stack.last().pose(), 0, 						0, 						  -90).uv(0, 0).endVertex();

        BufferUploader.drawWithShader(buffer.end());
		
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();

		stack.popPose();
	}

}
