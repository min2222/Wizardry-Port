package electroblob.wizardry.client;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

/**
 * Font renderer that renders parts of strings surrounded by '#' (without quotes) in the SGA instead of normal text.
 * 
 * @since Wizardry 1.1
 */
//@SideOnly(Side.CLIENT)
public class MixedFontRenderer extends Font {

	public MixedFontRenderer(Function<ResourceLocation, FontSet> function,
                             boolean p_i1035_4_){
		super(function, p_i1035_4_);
	}

	@Override
	public int drawShadow(PoseStack stack, String string, float x, float y, int colour, boolean shadow){

		int l = 0;

		boolean sga = false;

		while(string.indexOf('#') > -1){

			String section = string.substring(0, string.indexOf('#'));

			if(sga){
				l += Minecraft.getInstance().fontFilterFishy.drawShadow(stack, section, x, y, colour, shadow);
				x += Minecraft.getInstance().fontFilterFishy.width(section);
			}else{
				l += Minecraft.getInstance().font.drawShadow(stack, section, x, y, colour, shadow);
				x += Minecraft.getInstance().font.width(section);
			}

			string = string.substring(string.indexOf('#') + 1);
			sga = !sga;
		}

		if(sga){
			l += Minecraft.getInstance().fontFilterFishy.drawShadow(stack, string, x, y, colour, shadow);
		}else{
			l += Minecraft.getInstance().font.drawShadow(stack, string, x, y, colour, shadow);
		}

		return l;
	}

	@Override
	public int width(String string){

		int l = 0;

		boolean sga = false;

		while(string.indexOf('#') > -1){

			String section = string.substring(0, string.indexOf('#'));

			if(sga){
				l += Minecraft.getInstance().fontFilterFishy.width(section);
			}else{
				l += Minecraft.getInstance().font.width(section);
			}

			string = string.substring(string.indexOf('#') + 1);
			sga = !sga;
		}

		if(sga){
			l += Minecraft.getInstance().fontFilterFishy.width(string);
		}else{
			l += Minecraft.getInstance().font.width(string);
		}

		return l;
	}

	// This doesn't work the same way yet
	@Override
	public void drawSplitString(String string, int x, int y, int width, int colour){

		if(string.contains("#")){
			Minecraft.getInstance().fontFilterFishy.drawSplitString(string.substring(1), x, y, width,
					colour);
		}else{
			Minecraft.getInstance().fontRenderer.drawSplitString(string, x, y, width, colour);
		}
	}

}
