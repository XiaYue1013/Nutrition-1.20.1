package ca.wescook.nutrition.effects;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.capabilities.INutrientManager;
import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectsManager {

	public static class AppliedEffect {
		public final Effect effect;
		public final int amplifier;

		public AppliedEffect(Effect effect, int amplifier) {
			this.effect = effect;
			this.amplifier = amplifier;
		}
	}

	public static void reapplyEffects(Player player) {
		player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(manager -> {
			Map<Nutrient, Float> playerNutrition = manager.get();
			List<AppliedEffect> effects = removeDuplicates(getEffectsInThreshold(playerNutrition));

			for (AppliedEffect applied : effects) {
				Effect effect = applied.effect;
				if (effect.potion == null) continue;
				boolean ambient = (effect.particles == Effect.ParticleVisibility.TRANSLUCENT);
				boolean showParticles = (effect.particles == Effect.ParticleVisibility.TRANSLUCENT || effect.particles == Effect.ParticleVisibility.OPAQUE);
				player.addEffect(new MobEffectInstance(effect.potion, 619, applied.amplifier, ambient, showParticles, true));
			}
		});
	}

	public static List<AppliedEffect> getEffectsInThreshold(Map<Nutrient, Float> playerNutrition) {
		List<AppliedEffect> effectsInThreshold = new ArrayList<>();

		for (Effect effect : EffectsList.get()) {
			if (effect.detect == null) continue;

			switch (effect.detect.toLowerCase()) {
				case "any": {
					for (Nutrient nutrient : effect.nutrients) {
						Float value = playerNutrition.get(nutrient);
						if (value != null && value >= effect.minimum && value <= effect.maximum) {
							effectsInThreshold.add(new AppliedEffect(effect, effect.amplifier));
							break;
						}
					}
					break;
				}

				case "average": {
					float total = 0f;
					int count = 0;
					for (Nutrient nutrient : effect.nutrients) {
						Float value = playerNutrition.get(nutrient);
						if (value != null) {
							total += value;
							count++;
						}
					}

					float average = (count != 0) ? total / count : -1f;
					if (average >= effect.minimum && average <= effect.maximum) {
						effectsInThreshold.add(new AppliedEffect(effect, effect.amplifier));
					}
					break;
				}

				case "all": {
					boolean allWithinThreshold = !effect.nutrients.isEmpty();
					for (Nutrient nutrient : effect.nutrients) {
						Float value = playerNutrition.get(nutrient);
						if (value == null || !(value >= effect.minimum && value <= effect.maximum)) {
							allWithinThreshold = false;
							break;
						}
					}

					if (allWithinThreshold) {
						effectsInThreshold.add(new AppliedEffect(effect, effect.amplifier));
					}
					break;
				}

				case "cumulative": {
					int cumulativeCount = 0;
					for (Nutrient nutrient : effect.nutrients) {
						Float value = playerNutrition.get(nutrient);
						if (value != null && value >= effect.minimum && value <= effect.maximum) {
							cumulativeCount++;
						}
					}

					if (cumulativeCount > 0) {
						int computedAmplifier = (cumulativeCount * effect.cumulativeModifier) - 1;
						effectsInThreshold.add(new AppliedEffect(effect, Math.max(0, computedAmplifier)));
					}
					break;
				}
			}
		}

		return effectsInThreshold;
	}

	public static List<AppliedEffect> removeDuplicates(List<AppliedEffect> effectsInput) {
		Map<MobEffect, AppliedEffect> highestMap = new HashMap<>();
		for (AppliedEffect effect : effectsInput) {
			if (effect.effect.potion == null) continue;
			MobEffect potion = effect.effect.potion;
			if (!highestMap.containsKey(potion) || effect.amplifier > highestMap.get(potion).amplifier) {
				highestMap.put(potion, effect);
			}
		}
		return new ArrayList<>(highestMap.values());
	}
}
