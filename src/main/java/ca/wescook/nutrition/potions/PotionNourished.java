package ca.wescook.nutrition.potions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PotionNourished extends PotionCustom {
	public PotionNourished(boolean visibility, ResourceLocation icon) {
		super(MobEffectCategory.BENEFICIAL, 0x4CAF50, visibility, icon);
	}

	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		if (modifier.getId().equals(ModPotions.NOURISHMENT_HEALTH)) {
			return (amplifier + 1) * 1.0D;
		}
		return super.getAttributeModifierValue(amplifier, modifier);
	}
}
