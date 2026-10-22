package ca.wescook.nutrition.events;

import ca.wescook.nutrition.potions.ModPotions;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventRegistry {
	public static void register(IEventBus modBus) {
		ModPotions.MOB_EFFECTS.register(modBus);
	}
}
