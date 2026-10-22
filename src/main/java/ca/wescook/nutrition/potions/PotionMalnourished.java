package ca.wescook.nutrition.potions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PotionMalnourished extends PotionCustom {
	public PotionMalnourished(boolean visibility, ResourceLocation icon) {
		super(MobEffectCategory.HARMFUL, 0xE57373, visibility, icon);
	}

	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		if (modifier.getId().equals(ModPotions.MALNOURISHMENT_HEALTH)) {
			return -((amplifier + 1) * 1.0D);
		}
		return super.getAttributeModifierValue(amplifier, modifier);
	}
}
