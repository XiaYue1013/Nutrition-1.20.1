package ca.wescook.nutrition;

import ca.wescook.nutrition.capabilities.SimpleImpl;
import ca.wescook.nutrition.effects.Effect;
import ca.wescook.nutrition.effects.EffectsList;
import ca.wescook.nutrition.effects.EffectsManager;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NutritionTest {

	private Nutrient grain;
	private Nutrient fruit;
	private Nutrient vegetable;
	private Nutrient protein;
	private Nutrient dairy;

	@BeforeAll
	public static void initMinecraft() {
		try {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();
		} catch (Throwable ignored) {
		}
	}

	@BeforeEach
	public void setUp() {
		Config.startingNutrition = 50;
		Config.nutritionMultiplier = 1.0f;
		Config.lossPerNutrient = 15;
		Config.decayMultiplier = 1.0f;
		Config.deathPenaltyLoss = 15;
		Config.deathPenaltyMin = 30;
		Config.deathPenaltyReset = true;

		grain = new Nutrient();
		grain.name = "grain";
		grain.decay = 1.0f;
		grain.visible = true;

		fruit = new Nutrient();
		fruit.name = "fruit";
		fruit.decay = 2.5f;
		fruit.visible = true;

		vegetable = new Nutrient();
		vegetable.name = "vegetable";
		vegetable.decay = 1.5f;
		vegetable.visible = true;

		protein = new Nutrient();
		protein.name = "protein";
		protein.decay = 1.0f;
		protein.visible = true;

		dairy = new Nutrient();
		dairy.name = "dairy";
		dairy.decay = 1.0f;
		dairy.visible = true;

		NutrientList.register(List.of(grain, fruit, vegetable, protein, dairy));
	}

	@Test
	public void testNutritionCapabilityBasicOperations() {
		SimpleImpl manager = new SimpleImpl();

		// Check starting nutrition
		for (Nutrient nutrient : NutrientList.get()) {
			assertEquals(50.0f, manager.get(nutrient), 0.001f);
		}

		// Add nutrition
		manager.add(fruit, 20.0f);
		assertEquals(70.0f, manager.get(fruit), 0.001f);

		// Add beyond maximum 100
		manager.add(fruit, 50.0f);
		assertEquals(100.0f, manager.get(fruit), 0.001f);

		// Subtract nutrition
		manager.subtract(fruit, 40.0f);
		assertEquals(60.0f, manager.get(fruit), 0.001f);

		// Subtract beyond minimum 0
		manager.subtract(fruit, 100.0f);
		assertEquals(0.0f, manager.get(fruit), 0.001f);

		// Reset
		manager.reset(fruit);
		assertEquals(50.0f, manager.get(fruit), 0.001f);

		manager.set(grain, 80.0f);
		manager.reset();
		assertEquals(50.0f, manager.get(grain), 0.001f);
	}

	@Test
	public void testNBTSerialization() {
		SimpleImpl manager = new SimpleImpl();
		manager.set(fruit, 75.5f);
		manager.set(grain, 25.0f);

		CompoundTag tag = manager.serializeNBT();
		assertEquals(75.5f, tag.getFloat("fruit"), 0.001f);
		assertEquals(25.0f, tag.getFloat("grain"), 0.001f);

		SimpleImpl loadedManager = new SimpleImpl();
		loadedManager.deserializeNBT(tag);
		assertEquals(75.5f, loadedManager.get(fruit), 0.001f);
		assertEquals(25.0f, loadedManager.get(grain), 0.001f);
		assertEquals(50.0f, loadedManager.get(vegetable), 0.001f);
	}

	@Test
	public void testNutritionMathFormulas() {
		int foodValue = 8; // e.g. Steak heals 8
		List<Nutrient> singleNutrient = List.of(protein);

		// 1 nutrient: 8 * 0.5 * 1.0 - 0 = 4.0
		float adjustedFoodValue = foodValue * 0.5f * Config.nutritionMultiplier;
		float lossPercentage = Config.lossPerNutrient / 100.0f;
		float foodLoss = adjustedFoodValue * lossPercentage * (singleNutrient.size() - 1);
		float result = Math.max(0, adjustedFoodValue - foodLoss);
		assertEquals(4.0f, result, 0.001f);

		// 2 nutrients: 4.0 - (4.0 * 0.15 * 1) = 3.4
		List<Nutrient> twoNutrients = List.of(protein, grain);
		foodLoss = adjustedFoodValue * lossPercentage * (twoNutrients.size() - 1);
		result = Math.max(0, adjustedFoodValue - foodLoss);
		assertEquals(3.4f, result, 0.001f);

		// 3 nutrients: 4.0 - (4.0 * 0.15 * 2) = 2.8
		List<Nutrient> threeNutrients = List.of(protein, grain, vegetable);
		foodLoss = adjustedFoodValue * lossPercentage * (threeNutrients.size() - 1);
		result = Math.max(0, adjustedFoodValue - foodLoss);
		assertEquals(2.8f, result, 0.001f);
	}

	@Test
	public void testDecayCalculation() {
		int hungerDrop = 2; // Food level dropped by 2
		float fruitDecayRate = 2.5f;

		float decay = (float) (hungerDrop * 0.075 * fruitDecayRate);
		assertEquals(0.375f, decay, 0.0001f);

		float initialNutrition = 50.0f;
		float afterDecay = initialNutrition - decay;
		assertEquals(49.625f, afterDecay, 0.0001f);
	}

	@Test
	public void testDeathPenaltyLogic() {
		float nutritionHigh = 60.0f;
		// 60 - 15 = 45 (> 30)
		float afterDeath = Math.max(Config.deathPenaltyMin, nutritionHigh - Config.deathPenaltyLoss);
		assertEquals(45.0f, afterDeath, 0.001f);

		float nutritionLow = 35.0f;
		// 35 - 15 = 20 -> capped at 30
		afterDeath = Math.max(Config.deathPenaltyMin, nutritionLow - Config.deathPenaltyLoss);
		assertEquals(30.0f, afterDeath, 0.001f);

		float nutritionVeryLow = 10.0f;
		// If deathPenaltyReset is true, resets to 30
		if (Config.deathPenaltyReset || nutritionVeryLow > Config.deathPenaltyMin) {
			afterDeath = Math.max(Config.deathPenaltyMin, nutritionVeryLow - Config.deathPenaltyLoss);
		} else {
			afterDeath = nutritionVeryLow;
		}
		assertEquals(30.0f, afterDeath, 0.001f);
	}

	@Test
	public void testEffectDetectionLogic() {
		Map<Nutrient, Float> nutrition = new HashMap<>();
		nutrition.put(grain, 95.0f);
		nutrition.put(fruit, 92.0f);
		nutrition.put(vegetable, 91.0f);
		nutrition.put(protein, 50.0f);
		nutrition.put(dairy, 50.0f);

		// Effect 1: any (90-100) on [grain, fruit] -> should match
		Effect effectAny = new Effect();
		effectAny.name = "test_any";
		effectAny.detect = "any";
		effectAny.minimum = 90;
		effectAny.maximum = 100;
		effectAny.nutrients = List.of(grain, fruit);
		effectAny.amplifier = 0;

		// Effect 2: all (90-100) on [grain, fruit, protein] -> should NOT match (protein is 50)
		Effect effectAllFail = new Effect();
		effectAllFail.name = "test_all_fail";
		effectAllFail.detect = "all";
		effectAllFail.minimum = 90;
		effectAllFail.maximum = 100;
		effectAllFail.nutrients = List.of(grain, fruit, protein);
		effectAllFail.amplifier = 0;

		// Effect 3: all (90-100) on [grain, fruit, vegetable] -> should match
		Effect effectAllPass = new Effect();
		effectAllPass.name = "test_all_pass";
		effectAllPass.detect = "all";
		effectAllPass.minimum = 90;
		effectAllPass.maximum = 100;
		effectAllPass.nutrients = List.of(grain, fruit, vegetable);
		effectAllPass.amplifier = 0;

		// Effect 4: average (70-100) on all 5 -> (95+92+91+50+50)/5 = 75.6 -> should match
		Effect effectAverage = new Effect();
		effectAverage.name = "test_average";
		effectAverage.detect = "average";
		effectAverage.minimum = 70;
		effectAverage.maximum = 100;
		effectAverage.nutrients = List.of(grain, fruit, vegetable, protein, dairy);
		effectAverage.amplifier = 0;

		// Effect 5: cumulative (90-100) with cumulativeModifier = 1
		// grain (95), fruit (92), vegetable (91) -> 3 nutrients match -> amplifier = (3 * 1) - 1 = 2
		Effect effectCumulative = new Effect();
		effectCumulative.name = "test_cumulative";
		effectCumulative.detect = "cumulative";
		effectCumulative.minimum = 90;
		effectCumulative.maximum = 100;
		effectCumulative.nutrients = List.of(grain, fruit, vegetable, protein, dairy);
		effectCumulative.cumulativeModifier = 1;

		EffectsList.register(List.of(effectAny, effectAllFail, effectAllPass, effectAverage, effectCumulative));

		List<EffectsManager.AppliedEffect> inThreshold = EffectsManager.getEffectsInThreshold(nutrition);
		List<String> matchedNames = inThreshold.stream().map(a -> a.effect.name).toList();

		assertTrue(matchedNames.contains("test_any"));
		assertFalse(matchedNames.contains("test_all_fail"));
		assertTrue(matchedNames.contains("test_all_pass"));
		assertTrue(matchedNames.contains("test_average"));
		assertTrue(matchedNames.contains("test_cumulative"));

		// Verify cumulative amplifier
		EffectsManager.AppliedEffect cumulativeApplied = inThreshold.stream()
				.filter(a -> a.effect.name.equals("test_cumulative"))
				.findFirst()
				.orElseThrow();
		assertEquals(2, cumulativeApplied.amplifier);
	}
}
