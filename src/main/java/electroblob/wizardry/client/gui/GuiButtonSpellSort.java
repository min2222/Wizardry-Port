package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.util.ISpellSortable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiButtonSpellSort extends GuiButton {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/container/spell_sort_buttons.png");
	private static final int TEXTURE_WIDTH = 32;
	private static final int TEXTURE_HEIGHT = 32;

	public final ISpellSortable.SortType sortType;

	private final ISpellSortable sortable;
	private final Screen parent;

	public GuiButtonSpellSort(int id, int x, int y, ISpellSortable.SortType sortType, ISpellSortable sortable, Screen parent){
		super(id, x, y, 10, 10, I18n.format("container." + Wizardry.MODID + ":arcane_workbench.sort_" + sortType.name));
		this.sortType = sortType;
		this.sortable = sortable;
		this.parent = parent;
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){

		if(this.visible){

			// Whether the button is highlighted
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.getBbHeight();

			int k = 0;
			int l = this.sortType.ordinal() * this.getBbHeight();

			if(sortType == sortable.getSortType()){
				k += this.width;
				if(sortable.isSortDescending()) k += this.width;
			}

			parent.mc.getTextureManager().bindTexture(TEXTURE);
			DrawingUtils.drawTexturedRect(this.x, this.y, k, l, this.width, this.getBbHeight(), TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}
	}

	@Override
	public void drawButtonForegroundLayer(int mouseX, int mouseY){
		if(hovered) parent.drawHoveringText(this.displayString, mouseX, mouseY);
	}

}
