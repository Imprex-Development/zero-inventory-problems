package net.imprex.zip.config;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class BackpackTypeConfig {

	public String uniqueName;

	public Integer inventoryRows;

	public String displayName;
	public Integer customModelData;
	public String texture;
	public List<String> lore;

	public RecipeConfig recipe;

	public BackpackTypeConfig(ConfigurationSection config, String key) {
		if (config == null) {
			throw new IllegalArgumentException("Config section for backpack type " + key + " not found");
		}

		if (config.contains("uniqueName")) {
			this.uniqueName = config.getString("uniqueName");
		} else {
			throw new IllegalArgumentException("Config value uniqueName was not found in backpack type " + key);
		}

		if (config.contains("displayName")) {
			this.displayName = ChatColor.translateAlternateColorCodes('&', config.getString("displayName"));
		} else {
			throw new IllegalArgumentException("Config value displayName was not found in backpack type " + key);
		}

		if (config.contains("inventoryRows") && config.isInt("customModelData")) {
			this.inventoryRows = config.getInt("inventoryRows");
		} else {
			throw new IllegalArgumentException("Config value inventoryRows was not found in backpack type " + key);
		}

		if (config.contains("customModelData") && config.isInt("customModelData")) {
			this.customModelData = config.getInt("customModelData");
		} else {
			throw new IllegalArgumentException("Config value customModelData was not found in backpack type " + key);
		}

		if (config.contains("texture")) {
			this.texture = config.getString("texture");
		} else {
			throw new IllegalArgumentException("Config value texture was not found in backpack type " + key);
		}

		if (config.contains("lore") && config.isList("lore")) {
			this.lore = config.getStringList("lore").stream().map(lore -> ChatColor.translateAlternateColorCodes('&', lore)).toList();
		} else {
			throw new IllegalArgumentException("Config value lore was not found in backpack type " + key);
		}

		ConfigurationSection recipeSection = config.getConfigurationSection("recipe");
		RecipeConfig recipeConfig = new RecipeConfig(recipeSection, key);
		this.recipe = recipeConfig;
	}
}
