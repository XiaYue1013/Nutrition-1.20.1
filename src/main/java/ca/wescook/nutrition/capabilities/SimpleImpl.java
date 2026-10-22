package ca.wescook.nutrition.capabilities;

import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleImpl implements INutrientManager {
	private final Map<Nutrient, Float> nutrition = new HashMap<>();

	public SimpleImpl() {
		updateCapability();
	}

	@Override
	public Map<Nutrient, Float> get() {
		return nutrition;
	}

	@Override
	public Float get(Nutrient nutrient) {
		return nutrition.get(nutrient);
	}

	@Override
	public void set(Nutrient nutrient, Float value) {
		if (nutrient != null) {
			nutrition.put(nutrient, Mth.clamp(value, 0.0f, 100.0f));
		}
	}

	@Override
	public void set(Map<Nutrient, Float> nutrientData) {
		if (nutrientData != null) {
			for (Map.Entry<Nutrient, Float> entry : nutrientData.entrySet()) {
				if (entry.getKey() != null && entry.getValue() != null) {
					nutrition.put(entry.getKey(), Mth.clamp(entry.getValue(), 0.0f, 100.0f));
				}
			}
		}
	}

	@Override
	public void add(Nutrient nutrient, float amount) {
		if (nutrient == null) return;
		float current = nutrition.getOrDefault(nutrient, (float) Config.startingNutrition);
		nutrition.put(nutrient, Mth.clamp(current + amount, 0.0f, 100.0f));
	}

	@Override
	public void add(List<Nutrient> nutrientData, float amount) {
		if (nutrientData == null) return;
		for (Nutrient nutrient : nutrientData) {
			add(nutrient, amount);
		}
	}

	@Override
	public void subtract(Nutrient nutrient, float amount) {
		if (nutrient == null) return;
		float current = nutrition.getOrDefault(nutrient, (float) Config.startingNutrition);
		nutrition.put(nutrient, Mth.clamp(current - amount, 0.0f, 100.0f));
	}

	@Override
	public void subtract(List<Nutrient> nutrientData, float amount) {
		if (nutrientData == null) return;
		for (Nutrient nutrient : nutrientData) {
			subtract(nutrient, amount);
		}
	}

	@Override
	public void reset(Nutrient nutrient) {
		if (nutrient != null) {
			set(nutrient, (float) Config.startingNutrition);
		}
	}

	@Override
	public void reset() {
		for (Nutrient nutrient : NutrientList.get()) {
			reset(nutrient);
		}
	}

	@Override
	public void updateCapability() {
		Map<Nutrient, Float> nutritionOld = new HashMap<>(nutrition);
		nutrition.clear();

		loop:
		for (Nutrient nutrient : NutrientList.get()) {
			for (Map.Entry<Nutrient, Float> entry : nutritionOld.entrySet()) {
				if (nutrient.name.equals(entry.getKey().name)) {
					nutrition.put(nutrient, entry.getValue());
					continue loop;
				}
			}
			nutrition.put(nutrient, (float) Config.startingNutrition);
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		for (Nutrient nutrient : NutrientList.get()) {
			Float value = nutrition.get(nutrient);
			if (value != null) {
				tag.putFloat(nutrient.name, value);
			}
		}
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		Map<Nutrient, Float> loaded = new HashMap<>();
		for (Nutrient nutrient : NutrientList.get()) {
			float value;
			if (nbt.contains(nutrient.name)) {
				value = nbt.getFloat(nutrient.name);
			} else {
				value = (float) Config.startingNutrition;
			}
			loaded.put(nutrient, Mth.clamp(value, 0.0f, 100.0f));
		}
		set(loaded);
	}
}
