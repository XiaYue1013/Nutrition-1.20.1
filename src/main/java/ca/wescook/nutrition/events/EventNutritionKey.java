package ca.wescook.nutrition.events;

import ca.wescook.nutrition.gui.ModGuiHandler;
import ca.wescook.nutrition.proxy.ClientProxy;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class EventNutritionKey {

	@SubscribeEvent
	public void onKeyInput(InputEvent.Key event) {
		if (ClientProxy.keyNutritionGui == null) {
			return;
		}

		while (ClientProxy.keyNutritionGui.consumeClick()) {
			long windowHandle = Minecraft.getInstance().getWindow().getWindow();
			boolean isF3Down = InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_F3);
			if (!isF3Down && Minecraft.getInstance().screen == null) {
				ModGuiHandler.openNutritionGui();
			}
		}
	}
}
