package ca.wescook.nutrition.utility;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.network.Sync;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class ChatCommand {
	public enum actions { SET, ADD, SUBTRACT }

	private static final String HELP_STRING = "/nutrition <get/set/add/subtract/reset/reload> <player> <nutrient> <value>";

	private static final SuggestionProvider<CommandSourceStack> SUGGEST_NUTRIENTS = (context, builder) -> {
		List<String> nutrientNames = new ArrayList<>();
		for (Nutrient nutrient : NutrientList.get()) {
			nutrientNames.add(nutrient.name);
		}
		return SharedSuggestionProvider.suggest(nutrientNames, builder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nutrition")
						.requires(source -> source.hasPermission(2))
						.executes(context -> {
							context.getSource().sendSuccess(() -> Component.literal(HELP_STRING), false);
							return 1;
						})
						.then(Commands.literal("help").executes(context -> {
							context.getSource().sendSuccess(() -> Component.literal(HELP_STRING), false);
							return 1;
						}))
						.then(Commands.literal("reload").executes(context -> {
							DataImporter.reload();
							DataImporter.updatePlayerCapabilitiesOnServer(context.getSource().getServer());
							context.getSource().sendSuccess(() -> Component.literal("Nutrients and effects reloaded"), true);
							return 1;
						}))
						.then(Commands.literal("get")
								.then(Commands.argument("player", EntityArgument.player())
										.then(Commands.argument("nutrient", StringArgumentType.word())
												.suggests(SUGGEST_NUTRIENTS)
												.executes(context -> {
													ServerPlayer player = EntityArgument.getPlayer(context, "player");
													String nutrientName = StringArgumentType.getString(context, "nutrient");
													Nutrient nutrient = NutrientList.getByName(nutrientName);
													if (nutrient != null) {
														player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
															Float val = cap.get(nutrient);
															float num = (val != null) ? val : 0.0f;
															context.getSource().sendSuccess(() -> Component.literal(nutrient.name + ": " + String.format("%.2f", num) + "%"), false);
														});
													} else {
														context.getSource().sendFailure(Component.literal("'" + nutrientName + "' is not a valid nutrient."));
													}
													return 1;
												}))))
						.then(Commands.literal("set")
								.then(Commands.argument("player", EntityArgument.player())
										.then(Commands.argument("nutrient", StringArgumentType.word())
												.suggests(SUGGEST_NUTRIENTS)
												.then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 100.0f))
														.executes(context -> {
															ServerPlayer player = EntityArgument.getPlayer(context, "player");
															String nutrientName = StringArgumentType.getString(context, "nutrient");
															float value = FloatArgumentType.getFloat(context, "value");
															return executeAction(context.getSource(), player, nutrientName, value, actions.SET);
														})))))
						.then(Commands.literal("add")
								.then(Commands.argument("player", EntityArgument.player())
										.then(Commands.argument("nutrient", StringArgumentType.word())
												.suggests(SUGGEST_NUTRIENTS)
												.then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 100.0f))
														.executes(context -> {
															ServerPlayer player = EntityArgument.getPlayer(context, "player");
															String nutrientName = StringArgumentType.getString(context, "nutrient");
															float value = FloatArgumentType.getFloat(context, "value");
															return executeAction(context.getSource(), player, nutrientName, value, actions.ADD);
														})))))
						.then(Commands.literal("subtract")
								.then(Commands.argument("player", EntityArgument.player())
										.then(Commands.argument("nutrient", StringArgumentType.word())
												.suggests(SUGGEST_NUTRIENTS)
												.then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 100.0f))
														.executes(context -> {
															ServerPlayer player = EntityArgument.getPlayer(context, "player");
															String nutrientName = StringArgumentType.getString(context, "nutrient");
															float value = FloatArgumentType.getFloat(context, "value");
															return executeAction(context.getSource(), player, nutrientName, value, actions.SUBTRACT);
														})))))
						.then(Commands.literal("reset")
								.then(Commands.argument("player", EntityArgument.player())
										.executes(context -> {
											ServerPlayer player = EntityArgument.getPlayer(context, "player");
											player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
												cap.reset();
												Sync.serverRequest(player);
												context.getSource().sendSuccess(() -> Component.literal("Nutrition reset for " + player.getName().getString() + "!"), true);
											});
											return 1;
										})
										.then(Commands.argument("nutrient", StringArgumentType.word())
												.suggests(SUGGEST_NUTRIENTS)
												.executes(context -> {
													ServerPlayer player = EntityArgument.getPlayer(context, "player");
													String nutrientName = StringArgumentType.getString(context, "nutrient");
													Nutrient nutrient = NutrientList.getByName(nutrientName);
													if (nutrient != null) {
														player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
															cap.reset(nutrient);
															Sync.serverRequest(player);
															context.getSource().sendSuccess(() -> Component.literal("Nutrient " + nutrient.name + " reset for " + player.getName().getString() + "!"), true);
														});
													} else {
														context.getSource().sendFailure(Component.literal("'" + nutrientName + "' is not a valid nutrient."));
													}
													return 1;
												}))))
		);
	}

	private static int executeAction(CommandSourceStack source, ServerPlayer player, String nutrientName, float value, actions action) {
		Nutrient nutrient = NutrientList.getByName(nutrientName);
		if (nutrient == null) {
			source.sendFailure(Component.literal("'" + nutrientName + "' is not a valid nutrient."));
			return 0;
		}

		player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
			if (action == actions.SET) {
				cap.set(nutrient, value);
			} else if (action == actions.ADD) {
				cap.add(nutrient, value);
			} else if (action == actions.SUBTRACT) {
				cap.subtract(nutrient, value);
			}
			Sync.serverRequest(player);
			source.sendSuccess(() -> Component.literal(nutrient.name + " updated!"), true);
		});
		return 1;
	}
}
