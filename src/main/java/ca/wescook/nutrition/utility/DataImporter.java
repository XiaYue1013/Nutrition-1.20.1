package ca.wescook.nutrition.utility;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.effects.EffectsList;
import ca.wescook.nutrition.effects.JsonEffect;
import ca.wescook.nutrition.network.Sync;
import ca.wescook.nutrition.nutrients.JsonNutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DataImporter {
	private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();

	public static void reload() {
		NutrientList.register(DataParser.parseNutrients(loadJsonNutrients()));
		EffectsList.register(DataParser.parseEffects(loadJsonEffects()));

		if (Config.logMissingNutrients) {
			NutrientUtils.findRegisteredFoods();
		}
	}

	public static void updatePlayerCapabilitiesOnServer(MinecraftServer server) {
		if (server == null) return;
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
				cap.updateCapability();
				Sync.serverRequest(player);
			});
		}
	}

	private static List<JsonNutrient> loadJsonNutrients() {
		List<String> nutrientFiles = Lists.newArrayList("dairy.json", "example.json", "fruit.json", "grain.json", "protein.json", "vegetable.json");
		File configDir = (Config.configDirectory != null) ? Config.configDirectory : new File("config");
		File nutrientDirectory = new File(configDir, Nutrition.MODID + "/nutrients");
		createConfigurationDirectory("assets/nutrition/configs/nutrients", nutrientDirectory, nutrientFiles);
		return readConfigurationDirectory(JsonNutrient.class, nutrientDirectory);
	}

	private static List<JsonEffect> loadJsonEffects() {
		List<String> effectsFiles = Lists.newArrayList("example.json", "mining_fatigue.json", "resistance.json", "strength.json", "toughness.json", "weakness.json");
		File configDir = (Config.configDirectory != null) ? Config.configDirectory : new File("config");
		File effectsDirectory = new File(configDir, Nutrition.MODID + "/effects");
		createConfigurationDirectory("assets/nutrition/configs/effects", effectsDirectory, effectsFiles);
		return readConfigurationDirectory(JsonEffect.class, effectsDirectory);
	}

	private static void createConfigurationDirectory(String inputDirectory, File outputDirectory, List<String> files) {
		if (outputDirectory.exists())
			return;

		outputDirectory.mkdirs();

		ClassLoader loader = DataImporter.class.getClassLoader();
		for (String file : files) {
			String resourcePath = inputDirectory + "/" + file;
			try (InputStream inputStream = loader.getResourceAsStream(resourcePath)) {
				if (inputStream != null) {
					Files.copy(inputStream, new File(outputDirectory, file).toPath());
				} else {
					Log.warn("Could not find default config resource: " + resourcePath);
				}
			} catch (IOException e) {
				Log.error("Error creating default config file: " + file);
			}
		}
	}

	private static <T> List<T> readConfigurationDirectory(Class<T> classImport, File configDirectory) {
		File[] files = configDirectory.listFiles();
		List<T> jsonObjectList = new ArrayList<>();
		if (files == null) return jsonObjectList;

		for (File file : files) {
			if (FilenameUtils.isExtension(file.getName(), "json")) {
				try (FileReader fileReader = new FileReader(file); JsonReader jsonReader = new JsonReader(fileReader)) {
					T obj = gson.fromJson(jsonReader, classImport);
					if (obj != null) {
						jsonObjectList.add(obj);
					}
				} catch (Exception e) {
					Log.fatal("The file " + file.getName() + " has invalid JSON and could not be loaded: " + e.getMessage());
				}
			}
		}

		return jsonObjectList;
	}
}
