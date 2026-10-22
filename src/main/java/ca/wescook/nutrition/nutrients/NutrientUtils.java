package ca.wescook.nutrition.nutrients;

import ca.wescook.nutrition.utility.Config;
import ca.wescook.nutrition.utility.Log;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NutrientUtils {
	private static final Map<Item, List<Nutrient>> NUTRIENT_CACHE = new ConcurrentHashMap<>();

	public static void clearCache() {
		NUTRIENT_CACHE.clear();
	}

	public static List<Nutrient> getFoodNutrients(ItemStack eatingFood) {
		if (eatingFood.isEmpty()) {
			return Collections.emptyList();
		}

		Item item = eatingFood.getItem();
		boolean hasNbt = eatingFood.hasTag();
		if (!hasNbt && NUTRIENT_CACHE.containsKey(item)) {
			return NUTRIENT_CACHE.get(item);
		}

		List<Nutrient> nutrientsFound = new ArrayList<>();

		foodSearch:
		for (Nutrient nutrient : NutrientList.get()) {
			// Check direct resolved items
			if (nutrient.resolvedFoodItems.contains(item)) {
				nutrientsFound.add(nutrient);
				continue foodSearch;
			}

			// Check tags
			for (TagKey<Item> tag : nutrient.foodTags) {
				if (eatingFood.is(tag)) {
					nutrientsFound.add(nutrient);
					continue foodSearch;
				}
			}

			// Check item stacks
			for (ItemStack listedFood : nutrient.foodItems) {
				if (ItemStack.isSameItem(listedFood, eatingFood)) {
					nutrientsFound.add(nutrient);
					continue foodSearch;
				}
			}
		}

		if (!hasNbt) {
			NUTRIENT_CACHE.put(item, Collections.unmodifiableList(nutrientsFound));
		}

		return nutrientsFound;
	}

	public static float calculateNutrition(ItemStack itemStack, List<Nutrient> nutrients) {
		Item item = itemStack.getItem();
		int foodValue = 0;

		FoodProperties properties = itemStack.getFoodProperties(null);
		if (properties != null) {
			foodValue = properties.getNutrition();
		} else if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CakeBlock) {
			foodValue = 2;
		} else if (item == Items.MILK_BUCKET) {
			foodValue = 4;
		}

		float adjustedFoodValue = (float) (foodValue * 0.5f);
		adjustedFoodValue = adjustedFoodValue * Config.nutritionMultiplier;
		float lossPercentage = (float) Config.lossPerNutrient / 100.0f;
		float foodLoss = (adjustedFoodValue * lossPercentage * Math.max(0, nutrients.size() - 1));
		return Math.max(0.0f, adjustedFoodValue - foodLoss);
	}

	public static boolean isValidFood(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		}

		Item item = itemStack.getItem();
		if (item.isEdible() || itemStack.getFoodProperties(null) != null) {
			return true;
		}

		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CakeBlock) {
			return true;
		}

		if (item == Items.MILK_BUCKET) {
			return true;
		}

		return false;
	}

	public static void findRegisteredFoods() {
		for (Item item : ForgeRegistries.ITEMS) {
			ItemStack itemStack = new ItemStack(item);
			if (isValidFood(itemStack) && getFoodNutrients(itemStack).isEmpty()) {
				Log.warn("Registered food without nutrients: " + ForgeRegistries.ITEMS.getKey(item));
			}
		}
	}
}
