package ca.wescook.nutrition.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class Sync {
	public static void serverRequest(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			ModPacketHandler.NETWORK_CHANNEL.send(
					PacketDistributor.PLAYER.with(() -> serverPlayer),
					new PacketNutritionResponse.Message(serverPlayer)
			);
		}
	}

	public static void clientRequest() {
		ModPacketHandler.NETWORK_CHANNEL.sendToServer(new PacketNutritionRequest.Message());
	}
}
