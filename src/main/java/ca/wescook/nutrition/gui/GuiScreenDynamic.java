package ca.wescook.nutrition.gui;

import ca.wescook.nutrition.Nutrition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class GuiScreenDynamic extends Screen {
	private int guiWidth = 0;
	private int guiHeight = 0;

	public int top = 0;
	public int left = 0;
	public int right = 0;
	public int bottom = 0;

	protected final ResourceLocation GUI_BORDERS = new ResourceLocation(Nutrition.MODID, "textures/gui/gui.png");

	protected GuiScreenDynamic(Component title) {
		super(title);
	}

	protected void updateContainerSize(int guiWidth, int guiHeight) {
		this.guiWidth = guiWidth;
		this.guiHeight = guiHeight;

		top = (height / 2) - (guiHeight / 2);
		left = (width / 2) - (guiWidth / 2);
		right = (width / 2) + (guiWidth / 2);
		bottom = (height / 2) + (guiHeight / 2);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		drawDynamicBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}

	private void drawDynamicBackground(GuiGraphics guiGraphics) {
		// Top left corner
		guiGraphics.blit(GUI_BORDERS, left, top, 0, 0, 4, 4);

		// Bottom left corner
		guiGraphics.blit(GUI_BORDERS, left, bottom - 4, 0, 8, 4, 4);

		// Top right corner
		guiGraphics.blit(GUI_BORDERS, right - 4, top, 8, 0, 4, 4);

		// Bottom right corner
		guiGraphics.blit(GUI_BORDERS, right - 4, bottom - 4, 8, 8, 4, 4);

		// Left side
		for (int i = 0; i < guiHeight - 8; i += 4) {
			guiGraphics.blit(GUI_BORDERS, left, top + 4 + i, 0, 4, 4, 4);
		}

		// Top side
		for (int i = 0; i < guiWidth - 8; i += 4) {
			guiGraphics.blit(GUI_BORDERS, left + 4 + i, top, 4, 0, 4, 4);
		}

		// Right side
		for (int i = 0; i < guiHeight - 8; i += 4) {
			guiGraphics.blit(GUI_BORDERS, right - 4, top + 4 + i, 8, 4, 4, 4);
		}

		// Bottom side
		for (int i = 0; i < guiWidth - 8; i += 4) {
			guiGraphics.blit(GUI_BORDERS, left + 4 + i, bottom - 4, 4, 8, 4, 4);
		}

		// Center tiles
		for (int i = 0; i < guiWidth - 8; i += 4) {
			for (int j = 0; j < guiHeight - 8; j += 4) {
				guiGraphics.blit(GUI_BORDERS, left + 4 + i, top + 4 + j, 4, 4, 4, 4);
			}
		}
	}
}
