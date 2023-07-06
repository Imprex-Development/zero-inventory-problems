package net.imprex.zip.config.v2;

import java.lang.reflect.Field;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.BackpackTypeListConfig;
import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.config.MessageConfig;

public class BackpackConfig {

	private final BackpackPlugin plugin;

	private GeneralConfig generalConfig;
	private MessageConfig messageConfig;
	private BackpackTypeListConfig typeListConfig;

	public BackpackConfig(BackpackPlugin plugin) {
		this.plugin = plugin;
	}

	public void serialize(Object config) throws Exception {
		Objects.requireNonNull(config);

		ConfigSection section = this.getConfigSection(config.getClass());
		String sectionName = section.name();

		YamlConfiguration pluginConfig = (YamlConfiguration) this.plugin.getConfig();
		ConfigurationSection configSection = pluginConfig.getConfigurationSection(sectionName);
		if (configSection == null) {
			configSection = pluginConfig.createSection(sectionName);
		}

		Field[] fields = config.getClass().getFields();
		for (Field field : fields) {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if (entry == null) {
				continue;
			}

			String value = (String) field.get(config);
			configSection.set(entry.name(), value != null ? value : entry.defaultValue());
		}

		this.plugin.saveConfig();
	}

	public <T> T deserialize(Class<T> configClass) throws Exception {
		ConfigSection section = this.getConfigSection(configClass);
		String sectionName = section.name();

		YamlConfiguration pluginConfig = (YamlConfiguration) this.plugin.getConfig();
		ConfigurationSection configSection = pluginConfig.getConfigurationSection(sectionName);
		if (configSection == null) {
			configSection = pluginConfig.createSection(sectionName);
		}

		T config = configClass.getConstructor().newInstance();

		Field[] fields = configClass.getFields();
		for (Field field : fields) {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if (entry == null) {
				continue;
			}

			String value = (String) configSection.get(entry.name(), entry.defaultValue());
			field.set(config, value);
		}
		return config;
	}

	private ConfigSection getConfigSection(Class<?> configClass) {
		Objects.requireNonNull(configClass);

		ConfigSection[] sectionAnnotations = configClass.getAnnotationsByType(ConfigSection.class);
		if (sectionAnnotations.length != 1) {
			throw new IllegalArgumentException(configClass.getSimpleName() + " has no ConfigSection annotation.");
		}
		return sectionAnnotations[0];
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