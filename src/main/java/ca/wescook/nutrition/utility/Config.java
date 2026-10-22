package ca.wescook.nutrition.utility;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class Config {
	public static File configDirectory;

	// Public config values
	public static boolean enableDecay = true;
	public static float decayMultiplier = 1.0f;
	public static int deathPenaltyMin = 30;
	public static boolean deathPenaltyReset = true;
	public static int deathPenaltyLoss = 15;
	public static boolean allowOverEating = false;
	public static int lossPerNutrient = 15;
	public static float nutritionMultiplier = 1.0f;
	public static int startingNutrition = 50;
	public static boolean enableGui = true;
	public static boolean enableGuiButton = true;
	public static boolean enableTooltips = true;
	public static int buttonXPosition = 134;
	public static int buttonYPosition = 61;
	public static String buttonOrigin = "gui";
	public static String buttonAnchor = "top-left";
	public static boolean logMissingFood = false;
	public static boolean logMissingNutrients = false;

	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	private static final ForgeConfigSpec.BooleanValue ENABLE_DECAY;
	private static final ForgeConfigSpec.DoubleValue DECAY_MULTIPLIER;
	private static final ForgeConfigSpec.IntValue DEATH_PENALTY_MIN;
	private static final ForgeConfigSpec.BooleanValue DEATH_PENALTY_RESET;
	private static final ForgeConfigSpec.IntValue DEATH_PENALTY_LOSS;
	private static final ForgeConfigSpec.DoubleValue NUTRITION_MULTIPLIER;
	private static final ForgeConfigSpec.IntValue STARTING_NUTRITION;
	private static final ForgeConfigSpec.IntValue LOSS_PER_NUTRIENT;
	private static final ForgeConfigSpec.BooleanValue ALLOW_OVER_EATING;
	private static final ForgeConfigSpec.BooleanValue ENABLE_GUI;
	private static final ForgeConfigSpec.BooleanValue ENABLE_GUI_BUTTON;
	private static final ForgeConfigSpec.BooleanValue ENABLE_TOOLTIPS;
	private static final ForgeConfigSpec.ConfigValue<String> BUTTON_ORIGIN;
	private static final ForgeConfigSpec.ConfigValue<String> BUTTON_ANCHOR;
	private static final ForgeConfigSpec.IntValue BUTTON_X_POSITION;
	private static final ForgeConfigSpec.IntValue BUTTON_Y_POSITION;
	private static final ForgeConfigSpec.BooleanValue LOG_MISSING_FOOD;
	private static final ForgeConfigSpec.BooleanValue LOG_MISSING_NUTRIENTS;

	static {
		BUILDER.push("Nutrition Decay");
		ENABLE_DECAY = BUILDER.comment("Enable nutrition decay when hunger drains.").define("EnableDecay", true);
		DECAY_MULTIPLIER = BUILDER.comment("Global value to multiply decay rate by (eg. 0.5 halves the rate, 2.0 doubles it). This can also be set per-nutrient.")
				.defineInRange("DecayMultiplier", 1.0D, -100.0D, 100.0D);
		BUILDER.pop();

		BUILDER.push("Death Penalty");
		DEATH_PENALTY_MIN = BUILDER.comment("The minimum nutrition value that the death penalty may reduce to.")
				.defineInRange("DeathPenaltyMin", 30, 0, 100);
		DEATH_PENALTY_RESET = BUILDER.comment("On death, should nutrition be reset to DeathPenaltyMin if it's fallen below that value?\nThis is recommended to prevent death loops caused by negative effects.")
				.define("DeathPenaltyReset", true);
		DEATH_PENALTY_LOSS = BUILDER.comment("The nutrition value subtracted from each nutrient upon death.")
				.defineInRange("DeathPenaltyLoss", 15, 0, 100);
		BUILDER.pop();

		BUILDER.push("Nutrition");
		NUTRITION_MULTIPLIER = BUILDER.comment("Value to multiply base nutrition by for each food (eg. 0.5 to halve nutrition gain).")
				.defineInRange("NutritionMultiplier", 1.0D, 0.0D, 100.0D);
		STARTING_NUTRITION = BUILDER.comment("The starting nutrition level for new players.")
				.defineInRange("StartingNutrition", 50, 0, 100);
		LOSS_PER_NUTRIENT = BUILDER.comment("The nutrition value subtracted from foods per additional nutrient, as a percentage.\nThis is to prevent large, complex foods from being too powerful.\n(eg. 1 nutrient = 0% loss, 2 nutrients = 15% loss, 3 nutrients = 30% loss)")
				.defineInRange("LossPerNutrient", 15, 0, 100);
		ALLOW_OVER_EATING = BUILDER.comment("Allow player to continue eating even while full.\nThis setting may upset balance (and tummies), but is necessary for playing in peaceful mode.")
				.define("AllowOverEating", false);
		BUILDER.pop();

		BUILDER.push("Gui");
		ENABLE_GUI = BUILDER.comment("If the nutrition GUI should be enabled").define("EnableGui", true);
		ENABLE_GUI_BUTTON = BUILDER.comment("If the nutrition button should be shown on player inventory (hotkey will still function).").define("EnableGuiButton", true);
		ENABLE_TOOLTIPS = BUILDER.comment("If foods should show their nutrients on hover.").define("EnableTooltips", true);
		BUTTON_ORIGIN = BUILDER.comment("The origin defines the object which the nutrition button will be placed relative to.\nAccepted values: gui, screen")
				.define("ButtonOrigin", "gui");
		BUTTON_ANCHOR = BUILDER.comment("The anchor defines which side of the origin to position the button against.\nAccepted values: top, right, bottom, left, top-left, top-right, bottom-right, bottom-left, center")
				.define("ButtonAnchor", "top-left");
		BUTTON_X_POSITION = BUILDER.comment("The nutrition button's X position, relative to its anchor point.")
				.defineInRange("ButtonXPosition", 134, -1000, 1000);
		BUTTON_Y_POSITION = BUILDER.comment("The nutrition button's Y position, relative to its anchor point.")
				.defineInRange("ButtonYPosition", 61, -1000, 1000);
		BUILDER.pop();

		BUILDER.push("Logging");
		LOG_MISSING_FOOD = BUILDER.comment("Log foods which cannot be found but are still listed in nutrients file.").define("LogMissingFood", false);
		LOG_MISSING_NUTRIENTS = BUILDER.comment("Log foods which have been found but do not have any associated nutrients.").define("LogMissingNutrients", false);
		BUILDER.pop();

		SPEC = BUILDER.build();
	}

	public static void init() {
		configDirectory = FMLPaths.CONFIGDIR.get().toFile();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "nutrition/nutrition.toml");
	}

	public static void onConfigLoad(ModConfigEvent event) {
		if (event.getConfig().getSpec() == SPEC) {
			bake();
		}
	}

	public static void bake() {
		enableDecay = ENABLE_DECAY.get();
		decayMultiplier = DECAY_MULTIPLIER.get().floatValue();
		deathPenaltyMin = DEATH_PENALTY_MIN.get();
		deathPenaltyReset = DEATH_PENALTY_RESET.get();
		deathPenaltyLoss = DEATH_PENALTY_LOSS.get();
		nutritionMultiplier = NUTRITION_MULTIPLIER.get().floatValue();
		startingNutrition = STARTING_NUTRITION.get();
		lossPerNutrient = LOSS_PER_NUTRIENT.get();
		allowOverEating = ALLOW_OVER_EATING.get();
		enableGui = ENABLE_GUI.get();
		enableGuiButton = ENABLE_GUI_BUTTON.get();
		enableTooltips = ENABLE_TOOLTIPS.get();
		buttonOrigin = BUTTON_ORIGIN.get();
		buttonAnchor = BUTTON_ANCHOR.get();
		buttonXPosition = BUTTON_X_POSITION.get();
		buttonYPosition = BUTTON_Y_POSITION.get();
		logMissingFood = LOG_MISSING_FOOD.get();
		logMissingNutrients = LOG_MISSING_NUTRIENTS.get();
	}
}
