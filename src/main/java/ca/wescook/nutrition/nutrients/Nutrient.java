package ca.wescook.nutrition.nutrients;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Nutrient {
	public String name;
	public ItemStack icon;

	public ItemStack getIcon() {
		return icon != null ? icon : ItemStack.EMPTY;
	}
	public int color;
	public float decay;
	public boolean visible;
	public List<String> foodOreDict = new ArrayList<>();
	public List<ItemStack> foodItems = new ArrayList<>();

	// 1.20.1 item tags and resolved items for rapid matching
	public List<TagKey<Item>> foodTags = new ArrayList<>();
	public List<Item> resolvedFoodItems = new ArrayList<>();
}
