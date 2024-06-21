package electroblob.wizardry.potion;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/**
 * As of Wizardry 4.2, this class is used by all of wizardry's potions. Potions that work solely on events
 * instantiate this class directly, all other potions extend it.
 */
public class PotionMagicEffect extends MobEffect {

	private final ResourceLocation texture;

	public PotionMagicEffect(MobEffectCategory category, int liquidColour, ResourceLocation texture){
		super(category, liquidColour);
		this.texture = texture;
	}

	@Override
	public void applyEffectTick(LivingEntity entitylivingbase, int strength){
		// Nothing here because this potion works on events.
	}
	
	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new IClientMobEffectExtensions() {
			@Override
			public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
				RenderSystem.setShaderColor(1, 1, 1, alpha);
				drawIcon(poseStack, x + 3, y + 3, instance, Minecraft.getInstance());
				return true;
			}
			
			
			@Override
			public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
				drawIcon(poseStack, x + 6, y + 7, instance, Minecraft.getInstance());
				return true;
			}
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	protected void drawIcon(PoseStack poseStack, int x, int y, MobEffectInstance effect, net.minecraft.client.Minecraft mc){
		RenderSystem.setShaderTexture(0, texture);
		electroblob.wizardry.client.DrawingUtils.drawTexturedRect(poseStack, x, y, 0, 0, 18, 18, 18, 18);
	}

}
