package ca.wescook.nutrition.gui;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.network.Sync;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class NutritionGui extends GuiScreenDynamic {
	private Button buttonClose;

	// Magic Numbers
	private final int GUI_BASE_WIDTH = 184;
	private final int GUI_BASE_HEIGHT = 72;
	private final int NUTRITION_DISTANCE = 20;

	private final int TITLE_VERTICAL_OFFSET = 18;

	private final int NUTRITION_ICON_HORIZONTAL_OFFSET = 10;
	private final int NUTRITION_ICON_VERTICAL_OFFSET = 32;

	private final int NUTRITION_BAR_WIDTH = 130;
	private final int NUTRITION_BAR_HEIGHT = 13;
	private final int NUTRITION_BAR_HORIZONTAL_OFFSET = 40;
	private final int NUTRITION_BAR_VERTICAL_OFFSET = 33;

	private final int LABEL_NAME_HORIZONTAL_OFFSET = 30;
	private final int LABEL_VALUE_HORIZONTAL_OFFSET = 43;
	private final int LABEL_VERTICAL_OFFSET = 41;
	private int labelCharacterPadding = 0;

	private final int CLOSE_BUTTON_WIDTH = 70;
	private final int CLOSE_BUTTON_HEIGHT = 20;
	private final int CLOSE_BUTTON_OFFSET = 12;

	public NutritionGui() {
		super(Component.translatable("gui.nutrition:nutrition_title"));
	}

	@Override
	protected void init() {
		super.init();
		Sync.clientRequest();

		for (Nutrient nutrient : NutrientList.getVisible()) {
			int nutrientWidth = font.width(Component.translatable("nutrient." + Nutrition.MODID + ":" + nutrient.name));
			nutrientWidth = ((nutrientWidth + 3) / 4) * 4;
			if (nutrientWidth > labelCharacterPadding) {
				this.labelCharacterPadding = nutrientWidth;
			}
		}

		List<Nutrient> visibleNutrients = NutrientList.getVisible();
		int totalHeight = GUI_BASE_HEIGHT + (visibleNutrients.size() * NUTRITION_DISTANCE);
		updateContainerSize(GUI_BASE_WIDTH + labelCharacterPadding, totalHeight);

		this.buttonClose = addRenderableWidget(Button.builder(
				Component.translatable("gui.nutrition:close"),
				button -> onClose()
		).bounds(
				(width / 2) - (CLOSE_BUTTON_WIDTH / 2),
				bottom - CLOSE_BUTTON_HEIGHT - CLOSE_BUTTON_OFFSET,
				CLOSE_BUTTON_WIDTH,
				CLOSE_BUTTON_HEIGHT
		).build());
	}

	public void redrawLabels() {
		// Handled dynamically in render() for 1.20.1
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		// Draw title
		Component titleComp = Component.translatable("gui.nutrition:nutrition_title");
		guiGraphics.drawString(font, titleComp, (width / 2) - (font.width(titleComp) / 2), top + TITLE_VERTICAL_OFFSET, 0xffffffff, false);

		// Draw bars and nutrient info
		List<Nutrient> visibleNutrients = NutrientList.getVisible();
		for (int i = 0; i < visibleNutrients.size(); i++) {
			Nutrient nutrient = visibleNutrients.get(i);

			float currentVal = (ClientProxy.localNutrition != null && ClientProxy.localNutrition.get(nutrient) != null)
					? Math.round(ClientProxy.localNutrition.get(nutrient)) : 0.0f;
			int barDisplayWidth = (int) (currentVal / 100.0f * NUTRITION_BAR_WIDTH);

			// Draw icon
			guiGraphics.renderFakeItem(nutrient.getIcon(), left + NUTRITION_ICON_HORIZONTAL_OFFSET, top + NUTRITION_ICON_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE));

			// Black border
			guiGraphics.fill(
					left + NUTRITION_BAR_HORIZONTAL_OFFSET + labelCharacterPadding - 1,
					top + NUTRITION_BAR_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE) - 1,
					left + NUTRITION_BAR_HORIZONTAL_OFFSET + NUTRITION_BAR_WIDTH + labelCharacterPadding + 1,
					top + NUTRITION_BAR_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE) + NUTRITION_BAR_HEIGHT + 1,
					0xff000000
			);

			// Colored bar
			if (barDisplayWidth > 0) {
				guiGraphics.fill(
						left + NUTRITION_BAR_HORIZONTAL_OFFSET + labelCharacterPadding,
						top + NUTRITION_BAR_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE),
						left + NUTRITION_BAR_HORIZONTAL_OFFSET + barDisplayWidth + labelCharacterPadding,
						top + NUTRITION_BAR_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE) + NUTRITION_BAR_HEIGHT,
						nutrient.color
				);
			}

			// Nutrient name
			Component nutrientName = Component.translatable("nutrient." + Nutrition.MODID + ":" + nutrient.name);
			guiGraphics.drawString(font, nutrientName, left + LABEL_NAME_HORIZONTAL_OFFSET, top + LABEL_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE) - 5, 0xffffffff, false);

			// Percent value label
			String percentString;
			if (ClientProxy.localNutrition != null && ClientProxy.localNutrition.get(nutrient) != null) {
				percentString = Math.round(ClientProxy.localNutrition.get(nutrient)) + "%";
			} else {
				percentString = Component.translatable("gui.nutrition:updating").getString();
			}
			guiGraphics.drawString(font, percentString, left + LABEL_VALUE_HORIZONTAL_OFFSET + labelCharacterPadding + 2, top + LABEL_VERTICAL_OFFSET + (i * NUTRITION_DISTANCE) - 5, 0xffffffff, true);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE
				|| (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode))
				|| (ClientProxy.keyNutritionGui != null && ClientProxy.keyNutritionGui.matches(keyCode, scanCode))) {
			onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
