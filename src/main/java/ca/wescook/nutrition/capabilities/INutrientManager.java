package ca.wescook.nutrition.capabilities;

import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;

public interface INutrientManager {
	Map<Nutrient, Float> get();

	Float get(Nutrient nutrient);

	void set(Nutrient nutrient, Float value);

	void set(Map<Nutrient, Float> nutrientData);

	void add(Nutrient nutrient, float amount);

	void add(List<Nutrient> nutrientData, float amount);

	void subtract(Nutrient nutrient, float amount);

	void subtract(List<Nutrient> nutrientData, float amount);

	void reset(Nutrient nutrient);

	void reset();

	void updateCapability();

	CompoundTag serializeNBT();

	void deserializeNBT(CompoundTag nbt);
}
