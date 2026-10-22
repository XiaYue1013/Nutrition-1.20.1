package ca.wescook.nutrition.events;

import ca.wescook.nutrition.Nutrition;
import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.network.Sync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventPlayerJoinWorld {
	public static final ResourceLocation NUTRITION_CAP_KEY = new ResourceLocation(Nutrition.MODID, "nutrition");

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player) {
			CapabilityManager.Provider provider = new CapabilityManager.Provider();
			event.addCapability(NUTRITION_CAP_KEY, provider);
			event.addListener(provider::invalidate);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide()) {
			// Import the pack's former KubeJS values once. Keep the old compound as a backup.
			net.minecraft.nbt.CompoundTag data = player.getPersistentData();
			if (data.contains("sevtechNutrition", net.minecraft.nbt.Tag.TAG_COMPOUND)
					&& !data.getBoolean("nutrition:kubejs_imported")) {
				player.getCapability(CapabilityManager.NUTRITION_CAPABILITY).ifPresent(cap -> {
					cap.deserializeNBT(data.getCompound("sevtechNutrition"));
					data.putBoolean("nutrition:kubejs_imported", true);
				});
			}
			// The removed KubeJS effect used a different UUID; prevent a stale health bonus.
			net.minecraft.world.entity.ai.attributes.AttributeInstance health = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
			if (health != null) health.removeModifier(java.util.UUID.fromString("f42431e4-8efc-44d2-8249-fea2a2cb418e"));
			Sync.serverRequest(player);
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide()) {
			Sync.serverRequest(player);
		}
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide()) {
			Sync.serverRequest(player);
		}
	}
}
