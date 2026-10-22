package ca.wescook.nutrition.events;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.effects.EffectsManager;
import ca.wescook.nutrition.network.Sync;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientUtils;
import ca.wescook.nutrition.utility.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class EventEatFood {

	@SubscribeEvent
	public void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);

		boolean isCake = state.getBlock() instanceof CakeBlock;
		boolean isCandleCake = state.getBlock() instanceof CandleCakeBlock;
		if (!isCake && !isCandleCake) {
			return;
		}

		if (player.canEat(false) || Config.allowOverEating) {
			ItemStack cakeStack = new ItemStack(Items.CAKE);
			List<Nutrient> foundNutrients = NutrientUtils.getFoodNutrients(cakeStack);
			float nutritionValue = NutrientUtils.calculateNutrition(cakeStack, foundNutrients);

			if (!level.isClientSide()) {
				player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
					cap.add(foundNutrients, nutritionValue);
					Sync.serverRequest(player);
				});

				// Simulate cake eating if player was full but overeating is allowed
				if (!player.canEat(false) && Config.allowOverEating && isCake) {
					int bites = state.getValue(CakeBlock.BITES);
					if (bites < 6) {
						level.setBlock(pos, state.setValue(CakeBlock.BITES, bites + 1), 3);
					} else {
						level.removeBlock(pos, false);
					}
					player.getFoodData().eat(2, 0.1f);
				}
			}
		}
	}

	@SubscribeEvent
	public void startUsingItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		if (player.level().isClientSide()) return;

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty()) return;

		if (Config.allowOverEating && stack.isEdible() && !player.canEat(false)) {
			player.startUsingItem(event.getHand());
			event.setCancellationResult(InteractionResult.CONSUME);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void finishUsingItem(LivingEntityUseItemEvent.Finish event) {
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (player.level().isClientSide()) {
			return;
		}

		ItemStack itemStack = event.getItem();
		ItemStack dummyStack = itemStack.copy();
		dummyStack.setCount(1);

		applyNutrition(player, dummyStack);
		reapplyEffectsFromMilk(player, dummyStack);
	}

	private void applyNutrition(Player player, ItemStack itemStack) {
		if (!NutrientUtils.isValidFood(itemStack)) {
			return;
		}

		List<Nutrient> foundNutrients = NutrientUtils.getFoodNutrients(itemStack);
		float nutritionValue = NutrientUtils.calculateNutrition(itemStack, foundNutrients);

		player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
			cap.add(foundNutrients, nutritionValue);
			Sync.serverRequest(player);
		});
	}

	private void reapplyEffectsFromMilk(Player player, ItemStack itemStack) {
		if (itemStack.is(Items.MILK_BUCKET)) {
			EffectsManager.reapplyEffects(player);
		}
	}
}
