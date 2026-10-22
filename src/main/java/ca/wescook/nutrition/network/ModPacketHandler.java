package ca.wescook.nutrition.network;

import ca.wescook.nutrition.Nutrition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Nutrition.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private static int messageId = 0;

	public static void registerMessages() {
		NETWORK_CHANNEL.registerMessage(
				messageId++,
				PacketNutritionRequest.Message.class,
				PacketNutritionRequest.Message::encode,
				PacketNutritionRequest.Message::decode,
				PacketNutritionRequest.Handler::handle
		);

		NETWORK_CHANNEL.registerMessage(
				messageId++,
				PacketNutritionResponse.Message.class,
				PacketNutritionResponse.Message::encode,
				PacketNutritionResponse.Message::decode,
				PacketNutritionResponse.Handler::handle
		);
	}
}
