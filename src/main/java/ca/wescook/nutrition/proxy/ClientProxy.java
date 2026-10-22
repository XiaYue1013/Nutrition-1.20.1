package ca.wescook.nutrition.proxy;

import ca.wescook.nutrition.capabilities.INutrientManager;
import ca.wescook.nutrition.capabilities.SimpleImpl;
import ca.wescook.nutrition.events.EventNutritionButton;
import ca.wescook.nutrition.events.EventNutritionKey;
import ca.wescook.nutrition.events.EventTooltip;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.utility.Config;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "nutrition", bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ClientProxy implements IProxy {
	public static INutrientManager localNutrition = new SimpleImpl();
	public static KeyMapping keyNutritionGui;

	@Override
	public void preInit() {}

	@Override
	public void init() {
		if (Config.enableGui) {
			MinecraftForge.EVENT_BUS.register(new EventNutritionKey());
			if (Config.enableGuiButton) {
				MinecraftForge.EVENT_BUS.register(new EventNutritionButton());
			}
		}

		if (Config.enableTooltips) {
			MinecraftForge.EVENT_BUS.register(new EventTooltip());
		}
	}

	@Override
	public void postInit() {}

	@net.minecraftforge.eventbus.api.SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		keyNutritionGui = new KeyMapping(
				"key.nutrition",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"Nutrition"
		);
		event.register(keyNutritionGui);
	}

	public static void updateNutrition(Map<String, Float> clientNutrients) {
		if (localNutrition == null) {
			localNutrition = new SimpleImpl();
		}
		Map<Nutrient, Float> mapped = new HashMap<>();
		for (Map.Entry<String, Float> entry : clientNutrients.entrySet()) {
			Nutrient nutrient = NutrientList.getByName(entry.getKey());
			if (nutrient != null) {
				mapped.put(nutrient, entry.getValue());
			}
		}
		localNutrition.set(mapped);
	}
}
