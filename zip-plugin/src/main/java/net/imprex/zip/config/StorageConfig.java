package net.imprex.zip.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class StorageConfig {

	public LocalConfig local;

	public StorageConfig(ConfigurationSection config) {

		config.addDefault("local", new YamlConfiguration());
		ConfigurationSection localSection = config.getConfigurationSection("local");
		this.local = new LocalConfig(localSection);
	}

	public class LocalConfig {

		public String dataFolder;

		public LocalConfig(ConfigurationSection config) {
			config.addDefault("dataFolder", "plugins/ZeroInventoryProblems/storage");

			this.dataFolder = config.getString("dataFolder");
		}
	}
}