package dev.imprex.zip.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import dev.imprex.zip.BackpackPlugin;
import dev.imprex.zip.common.MinecraftVersion;

public class BackpackConfig {

	private final BackpackPlugin plugin;

	private GeneralConfig generalConfig;
	private MessageConfig messageConfig;
	private BackpackTypeListConfig typeListConfig;

	public BackpackConfig(BackpackPlugin plugin) {
		this.plugin = plugin;
	}

	public void deserialize() {
		try {
			Path dataFolder = this.plugin.getDataFolder().toPath();
			Path configPath = dataFolder.resolve("config.yml");

			if (Files.notExists(configPath)) {
				String configVersion = MinecraftVersion.majorVersion() + "." + MinecraftVersion.minorVersion();

				if (Files.notExists(dataFolder)) {
					Files.createDirectories(dataFolder);
				}

				Files.copy(BackpackPlugin.class.getResourceAsStream("/config/config-" + configVersion + ".yml"), configPath);
			}
		} catch (IOException e) {
			throw new RuntimeException("unable to create config", e);
		}

		FileConfiguration config = this.plugin.getConfig();

		ConfigurationSection generalSection = config.getConfigurationSection("general");
		this.generalConfig = new GeneralConfig(generalSection);

		ConfigurationSection backpackTypeListSection = config.getConfigurationSection("type");
		this.typeListConfig = new BackpackTypeListConfig(backpackTypeListSection);

		this.messageConfig = new MessageConfig(this.plugin);
	}

	public GeneralConfig general() {
		return this.generalConfig;
	}

	public MessageConfig message() {
		return this.messageConfig;
	}

	public BackpackTypeListConfig typeList() {
		return this.typeListConfig;
	}
}