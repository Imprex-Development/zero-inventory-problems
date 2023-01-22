package net.imprex.zip.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.util.ZIPLogger;

public class RecipeConfig {

	public Boolean discover;
	public String group;
	public String patternOne;
	public String patternTwo;
	public String patternThree;
	public Map<String, Material> patternMapping;

	public RecipeConfig(ConfigurationSection config, String key) {
		if (config == null) {
			throw new IllegalArgumentException("Config section for backpack recipe in " + key + " not found");
		}

		if (config.contains("discover") && config.isBoolean("discover")) {
			this.discover = config.getBoolean("discover");
		} else {
			throw new IllegalArgumentException("Config value discover was not found in backpack type " + key);
		}

		if (config.contains("group")) {
			String group = config.getString("group");
			this.group = group != null && !group.equalsIgnoreCase("null")
					? group
					: null;
		} else {
			throw new IllegalArgumentException("Config value group was not found in backpack type " + key);
		}

		if (config.contains("patternOne")) {
			this.patternOne = config.getString("patternOne");
		} else {
			throw new IllegalArgumentException("Config value patternOne was not found in backpack type " + key);
		}

		if (config.contains("patternTwo")) {
			this.patternTwo = config.getString("patternTwo");
		} else {
			throw new IllegalArgumentException("Config value patternTwo was not found in backpack type " + key);
		}

		if (config.contains("patternThree")) {
			this.patternThree = config.getString("patternThree");
		} else {
			throw new IllegalArgumentException("Config value patternThree was not found in backpack type " + key);
		}


		ConfigurationSection patternMappingSection = config.getConfigurationSection("patternMapping");
		Map<String, Material> patternMapping = this.loadBackpackRecipeMapping(patternMappingSection, key);
		this.patternMapping = patternMapping;
	}

	public Map<String, Material> loadBackpackRecipeMapping(ConfigurationSection config, String key) {
		if (config == null) {
			throw new IllegalArgumentException("Config value patternMapping was not found in backpack type " + key);
		}

		Map<String, Material> patternMapping = new HashMap<>();
		for (String mappingKey : config.getKeys(false)) {
			Material material = null;
			try {
				material = Material.valueOf(config.getString(mappingKey));
			} catch (Exception e) {
				ZIPLogger.warn(String.format("Config material value \"%s\" is not valid in backpack recipe type section \"%s\"",
						config.getString(mappingKey),
						key));
			}

			if (material == null || mappingKey.length() != 1) {
				throw new IllegalArgumentException(String.format("Config value \"%s\" is not valid in backpack recipe type section \"%s\"",
						mappingKey,
						key));
			}

			patternMapping.put(mappingKey, material);
		}

		return patternMapping;
	}
}
