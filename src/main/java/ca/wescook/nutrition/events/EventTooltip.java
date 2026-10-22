package ca.wescook.nutrition.events;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class EventTooltip {

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		ItemStack itemStack = event.getItemStack();
		if (itemStack.isEmpty() || !NutrientUtils.isValidFood(itemStack)) {
			return;
		}

		List<Nutrient> foundNutrients = NutrientUtils.getFoodNutrients(itemStack);
		if (foundNutrients.isEmpty()) {
			return;
		}

		StringJoiner stringJoiner = new StringJoiner(", ");
		for (Nutrient nutrient : foundNutrients) {
			if (nutrient.visible) {
				stringJoiner.add(Component.translatable("nutrient." + Nutrition.MODID + ":" + nutrient.name).getString());
			}
		}

		String nutrientString = stringJoiner.toString();
		if (nutrientString.isEmpty()) {
			return;
		}

		float nutritionValue = NutrientUtils.calculateNutrition(itemStack, foundNutrients);

		Component tooltip = Component.translatable("tooltip." + Nutrition.MODID + ":nutrients")
				.append(" ")
				.append(Component.literal(nutrientString).withStyle(ChatFormatting.DARK_GREEN))
				.append(Component.literal(" (" + String.format(Locale.ROOT, "%.1f", nutritionValue) + "%)").withStyle(ChatFormatting.DARK_AQUA));

		event.getToolTip().add(tooltip);
	}
}
