package ca.wescook.nutrition;

import ca.wescook.nutrition.capabilities.CapabilityManager;
import ca.wescook.nutrition.events.*;
import ca.wescook.nutrition.network.ModPacketHandler;
import ca.wescook.nutrition.potions.ModPotions;
import ca.wescook.nutrition.proxy.ClientProxy;
import ca.wescook.nutrition.proxy.IProxy;
import ca.wescook.nutrition.proxy.ServerProxy;
import ca.wescook.nutrition.utility.ChatCommand;
import ca.wescook.nutrition.utility.Config;
import ca.wescook.nutrition.utility.DataImporter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Nutrition.MODID)
public class Nutrition {
	public static final String MODID = "nutrition";
	public static final String MODNAME = "Nutrition";

	public static Nutrition instance;
	public static IProxy proxy;

	public Nutrition() {
		instance = this;
		proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

		Config.init();

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;

		ModPotions.createPotions();
		EventRegistry.register(modBus);

		modBus.addListener(Config::onConfigLoad);
		modBus.addListener(CapabilityManager::registerCapabilities);
		modBus.addListener(this::commonSetup);
		modBus.addListener(this::clientSetup);

		forgeBus.register(new EventPlayerJoinWorld());
		forgeBus.register(new EventPlayerDeath());
		forgeBus.register(new EventEatFood());
		forgeBus.register(new EventWorldTick());
		forgeBus.addListener(this::onRegisterCommands);
		forgeBus.addListener(this::onServerStarting);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			ModPacketHandler.registerMessages();
			CapabilityManager.register();
			DataImporter.reload();
			proxy.preInit();
		});
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			proxy.init();
		});
	}

	private void onRegisterCommands(RegisterCommandsEvent event) {
		ChatCommand.register(event.getDispatcher());
	}

	private void onServerStarting(ServerStartingEvent event) {
		DataImporter.reload();
		proxy.postInit();
	}
}
