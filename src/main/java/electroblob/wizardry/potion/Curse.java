package electroblob.wizardry.potion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

/** A <b>curse</b> is a permanent potion effect, which is displayed in the inventory with a special background and
 * no timer. It also allows for longer potion effect names by wrapping them onto two lines. */
public class Curse extends PotionMagicEffect {
	
	private static final ResourceLocation BACKGROUND = new ResourceLocation(Wizardry.MODID, "textures/gui/curse_background.png");

	public Curse(MobEffectCategory category, int liquidColour, ResourceLocation texture){
		super(category, liquidColour, texture);
	}
	
	@Override
	public List<ItemStack> getCurativeItems(){
		List<ItemStack> items = new ArrayList<>();
		items.add(new ItemStack(WizardryItems.PURIFYING_ELIXIR.get()));
		return items;
	}
	
	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new IClientMobEffectExtensions() {
			@Override
			public boolean renderInventoryText(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
				return true;
			}
			
			@Override
			public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
				Minecraft mc = Minecraft.getInstance();
				RenderSystem.setShaderTexture(0, BACKGROUND);
				electroblob.wizardry.client.DrawingUtils.drawTexturedRect(x, y, 0, 0, 140, 32, 256, 256);
				
				String name = Wizardry.proxy.translate(Curse.this.getDescriptionId());

				// Amplifier 0 (which would be I) is not rendered and the tooltips only go up to X (amplifier 9)
				// The vanilla implementation uses elseifs and only goes up to 4... how lazy.
				if(instance.getAmplifier() > 0 && instance.getAmplifier() < 10){
					name = name + " " + Wizardry.proxy.translate("enchantment.level." + (instance.getAmplifier() + 1));
				}

				List<String> lines = mc.font.split(name, 100);
				
				int i=0;
				for(String line : lines){
					int h = lines.size() == 1 ? 5 : i * (mc.font.lineHeight + 1);
					mc.font.drawShadow(poseStack, line, (float)(x + 10 + 18), (float)(y + 6 + h), 0xbf00ee);
					i++;
				}
				return IClientMobEffectExtensions.super.renderInventoryIcon(instance, screen, poseStack, x, y, blitOffset);
			}
			
			@Override
			public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
				RenderSystem.setShaderColor(1, 1, 1, 1);
				RenderSystem.setShaderTexture(0, BACKGROUND);
				electroblob.wizardry.client.DrawingUtils.drawTexturedRect(x, y, 141, 0, 24, 24, 256, 256);
				return IClientMobEffectExtensions.super.renderGuiIcon(instance, gui, poseStack, x, y, z, alpha);
			}
		});
	}
}
