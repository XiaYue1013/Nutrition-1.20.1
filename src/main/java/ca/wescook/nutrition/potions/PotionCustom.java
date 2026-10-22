package ca.wescook.nutrition.potions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

public class PotionCustom extends MobEffect {
	protected final boolean visibility;
	protected final ResourceLocation icon;

	public PotionCustom(MobEffectCategory category, int liquidColor, boolean visibility, ResourceLocation icon) {
		super(category, liquidColor);
		this.visibility = visibility;
		this.icon = icon;
	}

	public boolean isVisible() {
		return visibility;
	}

	public ResourceLocation getIcon() {
		return icon;
	}

	@Override
	public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
		consumer.accept(new IClientMobEffectExtensions() {
			@Override
			public boolean isVisibleInGui(MobEffectInstance effect) {
				return visibility;
			}

			@Override
			public boolean isVisibleInInventory(MobEffectInstance effect) {
				return visibility;
			}

			@Override
			public boolean renderInventoryIcon(MobEffectInstance instance, net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen<?> screen, net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int blitOffset) {
				if (icon != null) {
					guiGraphics.blit(icon, x + 6, y + 7, 0, 0, 18, 18, 18, 18);
					return true;
				}
				return false;
			}

			@Override
			public boolean renderGuiIcon(MobEffectInstance instance, net.minecraft.client.gui.Gui gui, net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
				if (icon != null) {
					guiGraphics.blit(icon, x + 3, y + 3, 0, 0, 18, 18, 18, 18);
					return true;
				}
				return false;
			}
		});
	}
}
