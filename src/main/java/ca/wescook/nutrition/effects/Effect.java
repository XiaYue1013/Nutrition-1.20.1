package ca.wescook.nutrition.effects;

import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class Effect {
	public String name;
	public MobEffect potion;
	public int amplifier;
	public int minimum;
	public int maximum;
	public String detect;
	public List<Nutrient> nutrients = new ArrayList<>();
	public int cumulativeModifier;
	public ParticleVisibility particles = ParticleVisibility.TRANSPARENT;

	public enum ParticleVisibility {
		OPAQUE,
		TRANSLUCENT,
		TRANSPARENT
	}
}
