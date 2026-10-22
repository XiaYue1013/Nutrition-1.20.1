package ca.wescook.nutrition.potions;

import ca.wescook.nutrition.Nutrition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class ModPotions {
	public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Nutrition.MODID);

	public static PotionToughness toughness;
	public static PotionMalnourished malnourished;
	public static PotionNourished nourished;

	public static RegistryObject<MobEffect> TOUGHNESS;
	public static RegistryObject<MobEffect> NOURISHED;
	public static RegistryObject<MobEffect> MALNOURISHED;

	static final UUID TOUGHNESS_HEALTH = UUID.fromString("d80b5ec3-8cf9-4b74-bc0d-6f3ef7b48b2e");
	static final UUID TOUGHNESS_ARMOR = UUID.fromString("f42431e4-8efc-44d2-8249-fea2a2cb418e");
	static final UUID TOUGHNESS_ATTACK_SPEED = UUID.fromString("10d42c27-f160-4909-a523-9b2553e14eac");
	static final UUID NOURISHMENT_HEALTH = UUID.fromString("bdafe0c7-5881-4505-802e-e18f6c419554");
	static final UUID MALNOURISHMENT_HEALTH = UUID.fromString("ea9cebf7-7c7a-4a89-a04f-221dab8ffdf7");

	public static void createPotions() {
		toughness = new PotionToughness(true, new ResourceLocation(Nutrition.MODID, "textures/potions/toughness.png"));
		toughness.addAttributeModifier(Attributes.MAX_HEALTH, TOUGHNESS_HEALTH.toString(), 0.0D, AttributeModifier.Operation.ADDITION);
		toughness.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ARMOR.toString(), 0.0D, AttributeModifier.Operation.ADDITION);
		toughness.addAttributeModifier(Attributes.ATTACK_SPEED, TOUGHNESS_ATTACK_SPEED.toString(), 0.0D, AttributeModifier.Operation.ADDITION);

		nourished = new PotionNourished(true, new ResourceLocation(Nutrition.MODID, "textures/potions/nourished.png"));
		nourished.addAttributeModifier(Attributes.MAX_HEALTH, NOURISHMENT_HEALTH.toString(), 0.0D, AttributeModifier.Operation.ADDITION);

		malnourished = new PotionMalnourished(true, new ResourceLocation(Nutrition.MODID, "textures/potions/malnourished.png"));
		malnourished.addAttributeModifier(Attributes.MAX_HEALTH, MALNOURISHMENT_HEALTH.toString(), 0.0D, AttributeModifier.Operation.ADDITION);

		TOUGHNESS = MOB_EFFECTS.register("toughness", () -> toughness);
		NOURISHED = MOB_EFFECTS.register("nourished", () -> nourished);
		MALNOURISHED = MOB_EFFECTS.register("malnourished", () -> malnourished);
	}
}
