package ca.wescook.nutrition.events;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class EventPlayerDeath {

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event) {
		Player original = event.getOriginal();
		Player newPlayer = event.getEntity();

		original.reviveCaps();
		try {
		original.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(oldCap -> {
			newPlayer.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(newCap -> {
				newCap.set(oldCap.get());

				if (event.isWasDeath()) {
					Map<Nutrient, Float> nutrition = newCap.get();
					for (Nutrient nutrient : nutrition.keySet()) {
						float current = nutrition.getOrDefault(nutrient, (float) Config.startingNutrition);
						if (Config.deathPenaltyReset || current > Config.deathPenaltyMin) {
							float penaltyVal = Math.max((float) Config.deathPenaltyMin, current - (float) Config.deathPenaltyLoss);
							newCap.set(nutrient, penaltyVal);
						}
					}
				}
			});
		});
		} finally {
			original.invalidateCaps();
		}
	}
}
