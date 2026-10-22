package ca.wescook.nutrition.gui;

import net.minecraft.client.Minecraft;

public class ModGuiHandler {
	public static final int NUTRITION_GUI_ID = 0;
	public static NutritionGui nutritionGui;

	public static void openNutritionGui() {
		Minecraft.getInstance().setScreen(nutritionGui = new NutritionGui());
	}
}
