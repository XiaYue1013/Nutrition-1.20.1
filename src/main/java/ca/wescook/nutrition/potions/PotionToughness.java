package ca.wescook.nutrition.potions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class PotionToughness extends PotionCustom {
	public PotionToughness(boolean visibility, ResourceLocation icon) {
		super(MobEffectCategory.BENEFICIAL, 0x8a9ea7, visibility, icon);
	}

	@Override
	public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
		if (modifier.getId().equals(ModPotions.TOUGHNESS_HEALTH)) {
			return (amplifier + 1) * 4.0D;
		}
		if (modifier.getId().equals(ModPotions.TOUGHNESS_ARMOR)) {
			return (amplifier + 1) * 2.0D;
		}
		if (modifier.getId().equals(ModPotions.TOUGHNESS_ATTACK_SPEED)) {
			return (amplifier + 1) * 0.1D;
		}
		return super.getAttributeModifierValue(amplifier, modifier);
	}
}
