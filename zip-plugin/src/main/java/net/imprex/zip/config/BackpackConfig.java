package net.imprex.zip.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.imprex.zip.BackpackPlugin;

public class BackpackConfig {

	private final BackpackPlugin plugin;

	private GeneralConfig generalConfig;
	private MessageConfig messageConfig;
	private BackpackTypeListConfig typeListConfig;

	public BackpackConfig(BackpackPlugin plugin) {
		this.plugin = plugin;
	}

	public void serialize() {
		FileConfiguration configuration = new YamlConfiguration();

		ConfigurationSection generalSection = configuration.createSection("general");
		this.generalConfig.save(generalSection);

		ConfigurationSection backpackTypeListSection = configuration.createSection("general");
		this.typeListConfig.save(backpackTypeListSection);

		try {
			Path dataFolder = this.plugin.getDataFolder().toPath();
			Path configPath = dataFolder.resolve("config.yml");

			if (Files.notExists(configPath)) {
				if (Files.notExists(dataFolder)) {
					Files.createDirectories(dataFolder);
				}

				configuration.save(configPath.toFile());
			}
		} catch (IOException e) {
			throw new RuntimeException("unable to create config", e);
		}
	}

	public void deserialize() {
		try {
			Path dataFolder = this.plugin.getDataFolder().toPath();
			Path configPath = dataFolder.resolve("config.yml");

			if (Files.notExists(configPath)) {
				if (Files.notExists(dataFolder)) {
					Files.createDirectories(dataFolder);
				}

				Files.copy(BackpackPlugin.class.getResourceAsStream("/config.yml"), configPath);
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