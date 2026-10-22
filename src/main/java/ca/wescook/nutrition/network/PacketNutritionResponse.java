package ca.wescook.nutrition.network;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.proxy.ClientProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketNutritionResponse {
	public static class Message {
		public Map<String, Float> clientNutrients = new HashMap<>();

		public Message() {}

		public Message(Player player) {
			player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(manager -> {
				for (Map.Entry<Nutrient, Float> entry : manager.get().entrySet()) {
					if (entry.getKey() != null && entry.getValue() != null) {
						clientNutrients.put(entry.getKey().name, entry.getValue());
					}
				}
			});
		}

		public static void encode(Message msg, FriendlyByteBuf buf) {
			buf.writeInt(msg.clientNutrients.size());
			for (Map.Entry<String, Float> entry : msg.clientNutrients.entrySet()) {
				buf.writeUtf(entry.getKey());
				buf.writeFloat(entry.getValue());
			}
		}

		public static Message decode(FriendlyByteBuf buf) {
			Message msg = new Message();
			int count = buf.readInt();
			for (int i = 0; i < count; i++) {
				String key = buf.readUtf();
				float val = buf.readFloat();
				msg.clientNutrients.put(key, val);
			}
			return msg;
		}
	}

	public static class Handler {
		public static void handle(Message msg, Supplier<NetworkEvent.Context> ctxSupplier) {
			NetworkEvent.Context ctx = ctxSupplier.get();
			ctx.enqueueWork(() -> {
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientProxy.updateNutrition(msg.clientNutrients));
			});
			ctx.setPacketHandled(true);
		}
	}
}
