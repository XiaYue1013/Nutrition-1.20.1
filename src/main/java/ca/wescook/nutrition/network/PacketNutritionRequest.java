package ca.wescook.nutrition.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNutritionRequest {
	public static class Message {
		public Message() {}

		public static void encode(Message msg, FriendlyByteBuf buf) {}

		public static Message decode(FriendlyByteBuf buf) {
			return new Message();
		}
	}

	public static class Handler {
		public static void handle(Message msg, Supplier<NetworkEvent.Context> ctxSupplier) {
			NetworkEvent.Context ctx = ctxSupplier.get();
			ctx.enqueueWork(() -> {
				ServerPlayer player = ctx.getSender();
				if (player != null) {
					Sync.serverRequest(player);
				}
			});
			ctx.setPacketHandled(true);
		}
	}
}
