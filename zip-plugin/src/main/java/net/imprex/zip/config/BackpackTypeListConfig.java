package net.imprex.zip.config;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public class BackpackTypeListConfig {

	public List<BackpackTypeConfig> type = new ArrayList<>();

	public BackpackTypeListConfig(ConfigurationSection config) {
		if (config == null) {
			throw new IllegalArgumentException("Config section type was not found");
		}

		for (String key : config.getKeys(false)) {
			try {
				ConfigurationSection backpackTypeSection = config.getConfigurationSection(key);
				BackpackTypeConfig backpackTypeConfig = new BackpackTypeConfig(backpackTypeSection, key);
				this.type.add(backpackTypeConfig);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
