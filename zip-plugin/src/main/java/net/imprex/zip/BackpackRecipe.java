package net.imprex.zip;

import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import net.imprex.zip.config.RecipeConfig;
import net.imprex.zip.config.BackpackTypeConfig;

public class BackpackRecipe extends ShapedRecipe {

	private boolean discover;

	public BackpackRecipe(BackpackPlugin plugin, BackpackTypeConfig config, ItemStack item) {
		super(plugin.createNamespacedKey("recipe." + config.uniqueName), item);
		RecipeConfig recipeConfig = config.recipe;

		this.discover = recipeConfig.discover;

		if (recipeConfig.group != null) {
			this.setGroup(recipeConfig.group);
		}

		this.shape(
				recipeConfig.patternOne,
				recipeConfig.patternTwo,
				recipeConfig.patternThree);

		for (Entry<String, Material> mapping : recipeConfig.patternMapping.entrySet()) {
			this.setIngredient(mapping.getKey().toCharArray()[0], mapping.getValue());
		}
	}

	public boolean canDiscover() {
		return this.discover;
	}
}