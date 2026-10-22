package ca.wescook.nutrition.effects;

import java.util.ArrayList;
import java.util.List;

public class EffectsList {
	private static final List<Effect> effects = new ArrayList<>();

	public static void register(List<Effect> effectsIn) {
		effects.clear();
		effects.addAll(effectsIn);
	}

	public static List<Effect> get() {
		return effects;
	}

	public static Effect getByName(String name) {
		for (Effect effect : effects) {
			if (effect.name.equals(name)) {
				return effect;
			}
		}
		return null;
	}
}
