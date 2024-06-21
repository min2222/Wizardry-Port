package electroblob.wizardry.client;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class containing some useful static methods for drawing GUIs, as well as general rendering.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see MixedFontRenderer
 */
//@SideOnly(Side.CLIENT)
public final class DrawingUtils {

	/**
	 * The integer colour for black passed into the font renderer methods. This used to be 0 but that's now white for
	 * some reason, so I've made a it a constant in case it changes again.
	 */
	// I think this is actually ever-so-slightly lighter than pure black, but the difference is unnoticeable.
	public static final int BLACK = 1;

	/**
	 * Shorthand for {@link DrawingUtils#drawTexturedRect(int, int, int, int, int, int, int, int)} which draws the
	 * entire texture (u and v are set to 0 and textureWidth and textureHeight are the same as width and height).
	 */
	public static void drawTexturedRect(PoseStack poseStack, int x, int y, int width, int height){
		drawTexturedRect(poseStack, x, y, 0, 0, width, height, width, height);
	}
	
	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into
	 * account, unlike {@link net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int, int, int, int)
	 * Gui.drawTexturedModalRect(int, int, int, int, int, int)}, which is harcoded for only 256x256 textures. Also handy
	 * for custom potion icons.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	public static void drawTexturedRect(PoseStack poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight){
		DrawingUtils.drawTexturedFlippedRect(poseStack, x, y, u, v, width, height, textureWidth, textureHeight, false, false);
	}

	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into
	 * account, unlike {@link net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int, int, int, int)
	 * Gui.drawTexturedModalRect(int, int, int, int, int, int)}, which is harcoded for only 256x256 textures. Also handy
	 * for custom potion icons. This version allows the texture to additionally be flipped in x and/or y.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 * @param flipX Whether to flip the texture in the x direction.
	 * @param flipY Whether to flip the texture in the y direction.
	 */
	public static void drawTexturedFlippedRect(PoseStack poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, boolean flipX, boolean flipY){
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		
		float f = 1F / (float)textureWidth;
		float f1 = 1F / (float)textureHeight;
		
		int u1 = flipX ? u + width : u;
		int u2 = flipX ? u : u + width;
		int v1 = flipY ? v + height : v;
		int v2 = flipY ? v : v + height;

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

		buffer.vertex(poseStack.last().pose(), (x), 		(y + height), 0).uv(((float)(u1) * f), ((float)(v2) * f1)).endVertex();
		buffer.vertex(poseStack.last().pose(), (x + width), (y + height), 0).uv(((float)(u2) * f), ((float)(v2) * f1)).endVertex();
		buffer.vertex(poseStack.last().pose(), (x + width), (y), 		  0).uv(((float)(u2) * f), ((float)(v1) * f1)).endVertex();
		buffer.vertex(poseStack.last().pose(), (x), 		(y), 		  0).uv(((float)(u1) * f), ((float)(v1) * f1)).endVertex();

        BufferUploader.drawWithShader(buffer.end());
	}

	/**
	 * Draws a textured rectangle, stretching the section of the image to fit the size given.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param v The y position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param finalWidth The width as rendered
	 * @param finalHeight The height as rendered
	 * @param width The width of the section, expressed as a fraction of the image width
	 * @param height The height of the section, expressed as a fraction of the image width
	 */
	public static void drawTexturedStretchedRect(PoseStack stack, int x, int y, int u, int v, int finalWidth, int finalHeight, int width,
			int height){
	
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(stack.last().pose(), (x), y + finalHeight, 0).uv(u, v + height).endVertex();
        buffer.vertex(stack.last().pose(), x + finalWidth, y + finalHeight, 0).uv(u + width, v + height).endVertex();
        buffer.vertex(stack.last().pose(), x + finalWidth, (y), 0).uv(u + width, v).endVertex();
        buffer.vertex(stack.last().pose(), (x), (y), 0).uv(u, v).endVertex();

        BufferUploader.drawWithShader(buffer.end());
	}

	/**
	 * Draws a 'glitch' rectangle, with some rows of pixels shifted randomly to give a broken effect.
	 *
	 * @param random A random number generator to use
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 * @param flipX Whether to flip the texture in the x direction.
	 * @param flipY Whether to flip the texture in the y direction.
	 */
	public static void drawGlitchRect(PoseStack stack, Random random, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, boolean flipX, boolean flipY){
		for(int i=0; i<height; i++){
			if(flipY) i = height - i - 1;
			int offset = random.nextInt(4) == 0 ? random.nextInt(6) - 3 : 0;
			drawTexturedFlippedRect(stack, x + offset, y + i, u, v + i, width, 1, textureWidth, textureHeight, flipX, flipY);
		}
	}

	/**
	 * Mixes the two given opaque colours in the proportion specified.
	 * @param colour1 The first colour to mix, as a 6-digit hexadecimal.
	 * @param colour2 The second colour to mix, as a 6-digit hexadecimal.
	 * @param proportion The proportion of the second colour; will be clamped to between 0 and 1.
	 * @return The resulting colour, as a 6-digit hexadecimal.
	 */
	public static int mix(int colour1, int colour2, float proportion){

		proportion = Mth.clamp(proportion, 0, 1);

		int r1 = colour1 >> 16 & 255;
		int g1 = colour1 >> 8 & 255;
		int b1 = colour1 & 255;
		int r2 = colour2 >> 16 & 255;
		int g2 = colour2 >> 8 & 255;
		int b2 = colour2 & 255;

		int r = (int)(r1 + (r2-r1) * proportion);
		int g = (int)(g1 + (g2-g1) * proportion);
		int b = (int)(b1 + (b2-b1) * proportion);

		return (r << 16) + (g << 8) + b;
	}

	/**
	 * Makes the given opaque colour translucent with the given opacity.
	 * @param colour An integer colour code, should be a 6-digit hexadecimal (i.e. opaque).
	 * @param opacity The opacity to apply to the given colour, as a fraction between 0 and 1.
	 * @return The resulting integer colour code, which will be an 8-digit hexadecimal.
	 */
	public static int makeTranslucent(int colour, float opacity){
		return colour + ((int)(opacity * 0xff) << 24);
	}

	/**
	 * Draws the given string at the given position, scaling it if it does not fit within the given width.
	 * @param font A {@code FontRenderer} object.
	 * @param text The text to display.
	 * @param x The x position of the top-left corner of the text.
	 * @param y The y position of the top-left corner of the text.
	 * @param scale The scale that the text should normally be if it does not exceed the maximum width.
	 * @param colour The colour to render the text in, supports translucency.
	 * @param width The maximum width of the text. <i>This is not scaled; you should pass in the width of the actual
	 * area of the screen in which the text needs to fit, regardless of the scale parameter.</i>
	 * @param centre Whether to adjust the y position such that the centre of the text lines up with where its centre
	 * would be if it was not scaled (automatically or manually).
	 * @param alignR True to right-align the text, false for normal left alignment.
	 */
	public static void drawScaledStringToWidth(PoseStack stack, Font font, String text, float x, float y, float scale, int colour, float width, boolean centre, boolean alignR){
		
		float textWidth = font.width(text) * scale;
		float textHeight = font.lineHeight * scale;
		
		if(textWidth > width){
			scale *= width/textWidth;
		}else if(alignR){ // Alignment makes no difference if the string fills the entire width
			x += width - textWidth;
		}
		
		if(centre) y += (font.lineHeight - textHeight)/2;
		
		DrawingUtils.drawScaledTranslucentString(stack, font, text, x, y, scale, colour);
	}

	/** Draws the given string at the given position, scaling the text by the specified factor. Also enables blending to
	 * render text in semitransparent colours (e.g. 0x88ffffff). */
	public static void drawScaledTranslucentString(PoseStack stack, Font font, String text, float x, float y, float scale, int colour){
		
        stack.pushPose();
        RenderSystem.enableBlend();
        stack.scale(scale, scale, scale);
        x /= scale;
        y /= scale;
        font.drawShadow(stack, text, x, y, colour);
        RenderSystem.disableBlend();
        stack.popPose();
	}

	/**
	 * Draws an itemstack and (optionally) its tooltip, directly. Mainly intended for use outside of GUI classes, since
	 * most of the GL state changes done in this method (which are the main reason it exists at all) are already done
	 * when drawing a GUI.
	 * 
	 * @param gui An instance of a GUI class.
	 * @param stack The itemstack to draw.
	 * @param x The x position of the left-hand edge of the itemstack.
	 * @param y The y position of the top edge of the itemstack.
	 * @param mouseX The x position of the mouse, used for tooltip positioning.
	 * @param mouseY The y position of the mouse, used for tooltip positioning.
	 * @param tooltip Whether to draw the tooltip.
	 */
	public static void drawItemAndTooltip(AbstractContainerScreen<?> gui, ItemStack stack, int x, int y, int mouseX, int mouseY, boolean tooltip){
	
        ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        renderItem.blitOffset = 100.0F;

        if (!stack.isEmpty()) {
            renderItem.renderGuiItem(stack, x, y);
            renderItem.renderGuiItemDecorations(Minecraft.getInstance().font, stack, x, y);

            if (tooltip) {
                gui.renderComponentTooltip(poseStack, gui.getTooltipFromItem(stack), mouseX + gui.getXSize() / 2 - gui.width / 2, mouseY + gui.getYSize() / 2 - gui.height / 2);
            }
        }

        poseStack.popPose();
	}

	/**
	 * Calculates a factor between 0 and 1 that results in a smooth, aesthetically-pleasing animation when used to scale
	 * things. Mainly for rendering, but can be used anywhere since it's just a mathematical formula.
	 * @param lifetime The lifetime of the thing being animated, in ticks (if this is negative, disappearing is ignored)
	 * @param tickCount The current age of the thing being animated, in ticks
	 * @param partialTicks The current partial tick time
	 * @param startLength The length of the appearing animation, in ticks
	 * @param endLength The length of the disappearing animation, in ticks
	 * @return A fraction between 0 and 1, with the value at the very start and end being 0 and the constant middle
	 * section having a value of 1.
	 */
	public static float smoothScaleFactor(int lifetime, int tickCount, float partialTicks, int startLength, int endLength){
		float age = tickCount + partialTicks;
		float s = Mth.clamp(age < startLength || lifetime < 0 ? age/startLength : (lifetime - age) / endLength, 0, 1);
		s = (float)Math.pow(s, 0.4); // Smooths the animation
		return s;
	}

	/**
	 * Tests if the point with the given coordinates lies within the rectangle specified. The check is identical to
	 * {@link AbstractContainerScreen#isPointInRegion(int, int, int, int, int, int)}, but without the adjustment relative to the GUI.
	 * @param left The minimum x coordinate of the rectangle
	 * @param top The minimum y coordinate of the rectangle
	 * @param width The width of the rectangle
	 * @param height The height of the rectangle
	 * @param x The x coordinate of the point to test
	 * @param y The y coordinate of the point to test
	 * @return True if the given point is in the rectangle, false otherwise
	 */
	public static boolean isPointInRegion(int left, int top, int width, int height, int x, int y){
		return x >= left - 1 && x < left + width + 1 && y >= top - 1 && y < top + height + 1;
	}

}
