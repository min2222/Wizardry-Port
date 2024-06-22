package electroblob.wizardry.client.renderer.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Vector3f;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class RenderSixthSense {

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_overlay.png");

	private static final ResourceLocation PASSIVE_MOB_MARKER_TEXTURE = 	new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_passive.png");
	private static final ResourceLocation HOSTILE_MOB_MARKER_TEXTURE = 	new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_hostile.png");
	private static final ResourceLocation PLAYER_MARKER_TEXTURE = 		new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_player.png");

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGuiOverlayEvent.Post event){

    	if(event.getOverlay() == VanillaGuiOverlay.HELMET.type()) {

			if(Minecraft.getInstance().player.hasEffect(WizardryPotions.SIXTH_SENSE.get())){

            	RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                WizardryClientEventHandler.renderScreenOverlay(event.getPoseStack(), event.getWindow(), SCREEN_OVERLAY_TEXTURE);
                
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
			}
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event){

        Minecraft mc = Minecraft.getInstance();
        PoseStack stack = event.getPoseStack();

		if(mc.player.hasEffect(WizardryPotions.SIXTH_SENSE.get()) && !(event.getEntity() instanceof ArmorStand)
				&& event.getEntity() != mc.player && mc.player.getEffect(WizardryPotions.SIXTH_SENSE.get()) != null
				&& event.getEntity().distanceTo(mc.player) < Spells.SIXTH_SENSE.getProperty(Spell.EFFECT_RADIUS).floatValue()
				* (1 + mc.player.getEffect(WizardryPotions.SIXTH_SENSE.get()).getAmplifier() * Constants.RANGE_INCREASE_PER_LEVEL)){

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();

            stack.pushPose();

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			// Disabling depth test allows it to be seen through everything.
			RenderSystem.disableDepthTest();

			stack.translate(0, event.getEntity().getBbHeight() * 0.6, 0);

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
            stack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

            RenderSystem.setShaderColor(1, 1, 1, 1);

            ResourceLocation texture = PASSIVE_MOB_MARKER_TEXTURE;

            if (ItemArtefact.isArtefactActive(mc.player, WizardryItems.CHARM_SIXTH_SENSE.get())) {
                if (event.getEntity() instanceof Enemy) texture = HOSTILE_MOB_MARKER_TEXTURE;
                else if (event.getEntity() instanceof Player) texture = PLAYER_MARKER_TEXTURE;
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);

            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            buffer.vertex(stack.last().pose(), -0.6f, 0.6f, 0).uv(0, 0).endVertex();
            buffer.vertex(stack.last().pose(), 0.6f, 0.6f, 0).uv(1, 0).endVertex();
            buffer.vertex(stack.last().pose(), 0.6f, -0.6f, 0).uv(1, 1).endVertex();
            buffer.vertex(stack.last().pose(), -0.6f, -0.6f, 0).uv(0, 1).endVertex();

            BufferUploader.drawWithShader(buffer.end());

            RenderSystem.enableCull();
            RenderSystem.disableBlend();

            stack.popPose();
		}

	}

}
