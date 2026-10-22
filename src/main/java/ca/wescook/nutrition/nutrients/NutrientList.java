package ca.wescook.nutrition.nutrients;

import java.util.ArrayList;
import java.util.List;

public class NutrientList {
	private static final List<Nutrient> nutrients = new ArrayList<>();

	public static void register(List<Nutrient> nutrientsIn) {
		nutrients.clear();
		nutrients.addAll(nutrientsIn);
		NutrientUtils.clearCache();
	}

	public static List<Nutrient> get() {
		return nutrients;
	}

	public static List<Nutrient> getVisible() {
		List<Nutrient> visibleNutrients = new ArrayList<>();
		for (Nutrient nutrient : nutrients) {
			if (nutrient.visible) {
				visibleNutrients.add(nutrient);
			}
		}
		return visibleNutrients;
	}

	public static Nutrient getByName(String name) {
		for (Nutrient nutrient : nutrients) {
			if (nutrient.name.equals(name)) {
				return nutrient;
			}
		}
		return null;
	}
}
