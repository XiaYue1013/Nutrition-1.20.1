package ca.wescook.nutrition.events;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.gui.ModGuiHandler;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventNutritionButton {
	private static final ResourceLocation NUTRITION_ICON = new ResourceLocation(Nutrition.MODID, "textures/gui/gui.png");

	@SubscribeEvent
	public void onGuiInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (!(screen instanceof InventoryScreen inventoryScreen)) {
			return;
		}

		if (!Config.enableGui || !Config.enableGuiButton) {
			return;
		}

		int[] pos = calculateButtonPosition(inventoryScreen);
		int x = pos[0];
		int y = pos[1];

		ImageButton buttonNutrition = new ImageButton(
				x, y, 20, 18, 14, 0, 19, NUTRITION_ICON,
				button -> ModGuiHandler.openNutritionGui()
		);

		event.addListener(buttonNutrition);
	}

	private int[] calculateButtonPosition(InventoryScreen gui) {
		int x = 0;
		int y = 0;
		int width = 0;
		int height = 0;

		if ("screen".equalsIgnoreCase(Config.buttonOrigin)) {
			width = gui.width;
			height = gui.height;
		} else {
			width = gui.getXSize();
			height = gui.getYSize();
		}

		switch (Config.buttonAnchor.toLowerCase()) {
			case "top" -> { x = width / 2; y = 0; }
			case "right" -> { x = width; y = height / 2; }
			case "bottom" -> { x = width / 2; y = height; }
			case "left" -> { x = 0; y = height / 2; }
			case "top-left" -> { x = 0; y = 0; }
			case "top-right" -> { x = width; y = 0; }
			case "bottom-right" -> { x = width; y = height; }
			case "bottom-left" -> { x = 0; y = height; }
			case "center" -> { x = width / 2; y = height / 2; }
			default -> { x = 0; y = 0; }
		}

		if ("gui".equalsIgnoreCase(Config.buttonOrigin)) {
			x += gui.getGuiLeft();
			y += gui.getGuiTop();
		}

		x += Config.buttonXPosition;
		y += Config.buttonYPosition;

		return new int[]{x, y};
	}
}
