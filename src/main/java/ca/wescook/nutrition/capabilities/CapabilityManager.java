package ca.wescook.nutrition.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityManager {
	public static final Capability<INutrientManager> NUTRITION_CAPABILITY = 
			net.minecraftforge.common.capabilities.CapabilityManager.get(new CapabilityToken<>() {});

	public static void registerCapabilities(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
		event.register(INutrientManager.class);
	}

	public static void register() {
		// Registered through CapabilityToken in Forge 1.20.1
	}

	public static class Provider implements ICapabilitySerializable<CompoundTag> {
		private final INutrientManager instance;
		private final LazyOptional<INutrientManager> optional;

		public Provider() {
			this.instance = new SimpleImpl();
			this.optional = LazyOptional.of(() -> this.instance);
		}

		public Provider(INutrientManager instance) {
			this.instance = instance;
			this.optional = LazyOptional.of(() -> this.instance);
		}

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
			if (cap == NUTRITION_CAPABILITY) {
				return optional.cast();
			}
			return LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT() {
			return Storage.writeNBT(instance);
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			Storage.readNBT(instance, nbt);
		}

		public void invalidate() {
			optional.invalidate();
		}
	}

	public static class Storage {
		public static CompoundTag writeNBT(INutrientManager instance) {
			return instance.serializeNBT();
		}

		public static void readNBT(INutrientManager instance, CompoundTag nbt) {
			instance.deserializeNBT(nbt);
		}
	}
}
