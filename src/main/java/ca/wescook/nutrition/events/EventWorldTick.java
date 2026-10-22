package ca.wescook.nutrition.events;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.effects.EffectsManager;
import ca.wescook.nutrition.network.Sync;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventWorldTick {
	private final Map<UUID, Integer> playerFoodLevels = new HashMap<>();
	private int potionCounter = 0;

	@SubscribeEvent
	public void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
			return;
		}

		if (Config.enableDecay) {
			for (Player player : event.level.players()) {
				nutritionDecay(player);
			}
		}

		if (event.level.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
			potionTicking(event.level.getServer());
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		playerFoodLevels.remove(event.getEntity().getUUID());
	}

	private void nutritionDecay(Player player) {
		UUID uuid = player.getUUID();
		int foodLevelNew = player.getFoodData().getFoodLevel();
		Integer foodLevelOld = playerFoodLevels.get(uuid);

		if (foodLevelOld != null && foodLevelNew < foodLevelOld) {
			int difference = foodLevelOld - foodLevelNew;

			player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
				Map<Nutrient, Float> nutrition = cap.get();
				for (Map.Entry<Nutrient, Float> entry : nutrition.entrySet()) {
					float decay = (float) (difference * 0.075 * entry.getKey().decay);
					entry.setValue(Mth.clamp(entry.getValue() - decay, 0.0f, 100.0f));
				}
				cap.set(nutrition);
				Sync.serverRequest(player);
			});
		}

		playerFoodLevels.put(uuid, foodLevelNew);
	}

	private void potionTicking(net.minecraft.server.MinecraftServer server) {
		if (server == null) return;

		potionCounter++;
		if (potionCounter > 110) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				EffectsManager.reapplyEffects(player);
			}
			potionCounter = 0;
		}
	}
}
