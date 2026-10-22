package ca.wescook.nutrition.utility;

import ca.wescook.nutrition.effects.Effect;
import ca.wescook.nutrition.effects.JsonEffect;
import ca.wescook.nutrition.nutrients.JsonNutrient;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;

public class DataParser {
	private static final Map<String, List<String>> ORE_DICT_TO_TAGS = new HashMap<>();

	static {
		ORE_DICT_TO_TAGS.put("listallfruit", List.of("forge:fruits", "forge:crops/fruit", "forge:fruits/apple"));
		ORE_DICT_TO_TAGS.put("cropapple", List.of("forge:fruits/apple", "forge:crops/apple", "forge:fruits"));
		ORE_DICT_TO_TAGS.put("listallveggie", List.of("forge:vegetables", "forge:crops/vegetable", "forge:salad_ingredients"));
		ORE_DICT_TO_TAGS.put("listallvegetable", List.of("forge:vegetables", "forge:crops/vegetable"));
		ORE_DICT_TO_TAGS.put("cropcarrot", List.of("forge:crops/carrot", "forge:vegetables/carrot"));
		ORE_DICT_TO_TAGS.put("croppotato", List.of("forge:crops/potato", "forge:vegetables/potato"));
		ORE_DICT_TO_TAGS.put("cropbeetroot", List.of("forge:crops/beetroot", "forge:vegetables/beetroot"));
		ORE_DICT_TO_TAGS.put("foodcheese", List.of("forge:cheese", "forge:cheeses", "forge:food/cheese"));
		ORE_DICT_TO_TAGS.put("foodbutter", List.of("forge:butter", "forge:food/butter"));
		ORE_DICT_TO_TAGS.put("foodchocolatebar", List.of("forge:chocolate", "forge:chocolates", "forge:food/chocolate"));
		ORE_DICT_TO_TAGS.put("listallmilk", List.of("forge:milk", "forge:milks", "forge:milk/milk_bottle"));
		ORE_DICT_TO_TAGS.put("listallmeat", List.of("forge:meat", "forge:meats", "forge:cooked_meat", "forge:raw_meat"));
		ORE_DICT_TO_TAGS.put("listallmeatcooked", List.of("forge:cooked_meat", "forge:cooked_meats"));
		ORE_DICT_TO_TAGS.put("listallmeatraw", List.of("forge:raw_meat", "forge:raw_meats"));
		ORE_DICT_TO_TAGS.put("listallfish", List.of("forge:fish", "forge:fishes", "forge:cooked_fishes", "forge:raw_fishes"));
		ORE_DICT_TO_TAGS.put("listallfishcooked", List.of("forge:cooked_fishes", "forge:cooked_fish"));
		ORE_DICT_TO_TAGS.put("listallfishraw", List.of("forge:raw_fishes", "forge:raw_fish"));
		ORE_DICT_TO_TAGS.put("listallgrain", List.of("forge:grain", "forge:grains", "forge:crops/wheat", "forge:bread"));
		ORE_DICT_TO_TAGS.put("cropwheat", List.of("forge:crops/wheat", "forge:grain/wheat"));
		ORE_DICT_TO_TAGS.put("bread", List.of("forge:bread", "forge:breads", "forge:food/bread"));
		ORE_DICT_TO_TAGS.put("listallegg", List.of("forge:eggs", "forge:egg"));
		ORE_DICT_TO_TAGS.put("egg", List.of("forge:eggs", "forge:egg"));
	}

	public static List<Nutrient> parseNutrients(List<JsonNutrient> jsonNutrients) {
		List<Nutrient> nutrients = new ArrayList<>();

		for (JsonNutrient nutrientRaw : jsonNutrients) {
			if (nutrientRaw.enabled != null && !nutrientRaw.enabled)
				continue;

			Nutrient nutrient = new Nutrient();

			try {
				nutrient.name = nutrientRaw.name;
				ResourceLocation iconLoc = new ResourceLocation(nutrientRaw.icon);
				Item iconItem = ForgeRegistries.ITEMS.getValue(iconLoc);
				nutrient.icon = (iconItem != null && iconItem != Items.AIR) ? new ItemStack(iconItem) : new ItemStack(Items.APPLE);
				nutrient.color = Integer.parseUnsignedInt("ff" + nutrientRaw.color, 16);
			} catch (Exception e) {
				Log.fatal("Missing or invalid JSON. A name, icon, and color are required for nutrient.");
				throw new IllegalArgumentException("Invalid nutrient JSON for " + nutrientRaw.name, e);
			}

			if (nutrientRaw.decay == null) {
				nutrient.decay = Config.decayMultiplier;
			} else if (nutrientRaw.decay >= -100 && nutrientRaw.decay <= 100) {
				nutrient.decay = nutrientRaw.decay;
			} else {
				nutrient.decay = 0;
				Log.error("Decay rate must be between -100 and 100 (" + nutrient.name + ").");
				continue;
			}

			nutrient.visible = (nutrientRaw.visible == null || nutrientRaw.visible);

			if (nutrientRaw.food.oredict != null) {
				nutrient.foodOreDict = nutrientRaw.food.oredict;
				for (String entry : nutrientRaw.food.oredict) {
					resolveOreDictEntry(entry, nutrient.foodTags);
				}
			}

			if (nutrientRaw.food.items != null) {
				for (String fullName : nutrientRaw.food.items) {
					if (fullName == null) {
						Log.warn("Null item in nutrient '" + nutrient.name + "' JSON.");
						continue;
					}

					String name = fullName;
					int metadata = 0;

					if (StringUtils.countMatches(fullName, ":") == 2) {
						name = StringUtils.substringBeforeLast(fullName, ":");
						String metaString = StringUtils.substringAfterLast(fullName, ":");
						if (NumberUtils.isCreatable(metaString)) {
							metadata = Integer.decode(metaString);
						}
					}

					// Check legacy item conversions
					name = convertLegacyItem(name, metadata);

					ResourceLocation loc = ResourceLocation.tryParse(name);
					if (loc == null) {
						continue;
					}

					Item item = ForgeRegistries.ITEMS.getValue(loc);
					if (item == null || item == Items.AIR) {
						String modid = loc.getNamespace();
						if (Config.logMissingFood && ModList.get().isLoaded(modid)) {
							Log.warn("Food with nutrients doesn't exist: " + fullName + " (" + nutrient.name + ")");
						}
						continue;
					}

					ItemStack itemStack = new ItemStack(item);
					if (NutrientUtils.isValidFood(itemStack)) {
						nutrient.foodItems.add(itemStack);
						if (!nutrient.resolvedFoodItems.contains(item)) {
							nutrient.resolvedFoodItems.add(item);
						}
					} else {
						Log.warn(name + " is not a valid food (" + fullName + ")");
					}
				}
			}

			nutrients.add(nutrient);
		}

		return nutrients;
	}

	private static void resolveOreDictEntry(String entry, List<TagKey<Item>> tags) {
		String clean = entry.startsWith("#") ? entry.substring(1) : entry;
		if (clean.contains(":")) {
			ResourceLocation loc = ResourceLocation.tryParse(clean);
			if (loc != null) {
				tags.add(TagKey.create(Registries.ITEM, loc));
			}
			return;
		}

		String key = clean.toLowerCase();
		if (ORE_DICT_TO_TAGS.containsKey(key)) {
			for (String tagStr : ORE_DICT_TO_TAGS.get(key)) {
				tags.add(TagKey.create(Registries.ITEM, new ResourceLocation(tagStr)));
			}
		}

		// Also register generic forge tag fallback
		tags.add(TagKey.create(Registries.ITEM, new ResourceLocation("forge", key)));
	}

	private static String convertLegacyItem(String name, int meta) {
		if ("minecraft:golden_apple".equals(name) && meta == 1) {
			return "minecraft:enchanted_golden_apple";
		}
		if ("minecraft:fish".equals(name)) {
			return switch (meta) {
				case 1 -> "minecraft:salmon";
				case 2 -> "minecraft:tropical_fish";
				case 3 -> "minecraft:pufferfish";
				default -> "minecraft:cod";
			};
		}
		if ("minecraft:cooked_fish".equals(name)) {
			return (meta == 1) ? "minecraft:cooked_salmon" : "minecraft:cooked_cod";
		}
		return name;
	}

	public static List<Effect> parseEffects(List<JsonEffect> jsonEffects) {
		List<Effect> effects = new ArrayList<>();

		for (JsonEffect effectRaw : jsonEffects) {
			if (effectRaw.enabled != null && !effectRaw.enabled)
				continue;

			ResourceLocation potionLoc = ResourceLocation.tryParse(effectRaw.potion);
			if (potionLoc == null) {
				Log.error("Potion identifier '" + effectRaw.potion + "' is invalid (" + effectRaw.name + ").");
				continue;
			}

			MobEffect potion = ForgeRegistries.MOB_EFFECTS.getValue(potionLoc);
			if (potion == null) {
				Log.error("Potion '" + effectRaw.potion + "' is not registered (" + effectRaw.name + ").");
				continue;
			}

			Effect effect = new Effect();
			effect.name = effectRaw.name;
			effect.potion = potion;
			effect.minimum = effectRaw.minimum;
			effect.maximum = effectRaw.maximum;
			effect.detect = effectRaw.detect;
			effect.amplifier = (effectRaw.amplifier != null) ? effectRaw.amplifier : 0;
			effect.cumulativeModifier = (effectRaw.cumulative_modifier != null) ? effectRaw.cumulative_modifier : 1;

			try {
				effect.particles = (effectRaw.particles != null) 
						? Effect.ParticleVisibility.valueOf(effectRaw.particles.toUpperCase()) 
						: Effect.ParticleVisibility.TRANSPARENT;
			} catch (IllegalArgumentException e) {
				Log.error("Particle visibility of '" + effect.name + "' is invalid. Skipping effect.");
				continue;
			}

			if (effectRaw.nutrients == null || effectRaw.nutrients.isEmpty()) {
				effect.nutrients.addAll(NutrientList.get());
			} else {
				for (String nutrientName : effectRaw.nutrients) {
					Nutrient nutrient = NutrientList.getByName(nutrientName);
					if (nutrient != null) {
						effect.nutrients.add(nutrient);
					} else {
						Log.error("Nutrient '" + nutrientName + "' not found (" + effectRaw.name + ").");
					}
				}
			}

			effects.add(effect);
		}

		return effects;
	}
}
