package net.imprex.zip.config.v2;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.BackpackTypeListConfig;
import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.v2.converter.ConfigConverter;
import net.imprex.zip.config.v2.converter.IntegerConverter;
import net.imprex.zip.config.v2.converter.ListConverter;
import net.imprex.zip.config.v2.converter.ObjectConverter;
import net.imprex.zip.config.v2.converter.StringConverter;

public class BackpackConfig {

	private final BackpackPlugin plugin;

	private final Map<Class<?>, ConfigConverter<?>> converterList = new HashMap<>();
	private final ConfigConverter<Object> defaultConverter = new ObjectConverter();

	private GeneralConfig generalConfig;
	private MessageConfig messageConfig;
	private BackpackTypeListConfig typeListConfig;

	public BackpackConfig(BackpackPlugin plugin) {
		this.plugin = plugin;

		this.registerConverter(new StringConverter());
		this.registerConverter(new IntegerConverter());
		this.registerConverter(new ListConverter());
	}

	private void registerConverter(ConfigConverter<?> converter) {
		for (Class<?> type : converter.getType()) {
			this.converterList.put(type, converter);
		}
	}

	public void serialize(Object configInstance) throws Exception {
		this.serialize(configInstance, true);
	}

	public void serialize(Object configInstance, boolean save) throws Exception {
		Objects.requireNonNull(configInstance);

		FileConfiguration config = this.plugin.getConfig();
		ConfigSection section = this.getConfigSection(configInstance.getClass(), true);
		String sectionName = section.name();

		ConfigurationSection childSection = config.getConfigurationSection(sectionName);
		if (childSection == null) {
			childSection = config.createSection(sectionName);
		}

		this.serializeClass(childSection, configInstance);

		if (save) {
			this.plugin.saveConfig();
		}
	}

	public <T> T deserialize(Class<T> configClass) throws Exception {
		Objects.requireNonNull(configClass);

		FileConfiguration config = this.plugin.getConfig();
		ConfigSection section = this.getConfigSection(configClass, true);
		String sectionName = section.name();

		ConfigurationSection childSection = config.getConfigurationSection(sectionName);
		if (childSection == null) {
			childSection = config.createSection(sectionName);
		}

		return this.deserializeClass(childSection, configClass);
	}

	public void serializeClass(ConfigurationSection config, Object configInstance) throws Exception {
		Objects.requireNonNull(config);
		Objects.requireNonNull(configInstance);

		Field[] fields = configInstance.getClass().getFields();
		for (Field field : fields) {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if (entry == null) {
				continue;
			}

			ConfigurationSection entryConfig = config;
			Object value = field.get(configInstance);

			ConfigSection entrySection = this.getConfigSection(field.getType(), false);
			if (entrySection != null) {
				String entrySectionName = entrySection.name();
				entryConfig = config.getConfigurationSection(entrySectionName);
				if (entryConfig == null) {
					entryConfig = config.createSection(entrySectionName);
				}

				this.serializeClass(entryConfig, value);
				continue;
			}

			ConfigConverter<Object> converter = this.getConverter(field.getType());
			converter.serialize(config, entry, value);
		}
	}

	public <T> T deserializeClass(ConfigurationSection config, Class<T> configClass) throws Exception {
		Objects.requireNonNull(config);
		Objects.requireNonNull(configClass);

		T configInstance = configClass.getConstructor().newInstance();

		Field[] fields = configClass.getFields();
		for (Field field : fields) {
			ConfigEntry entry = field.getAnnotation(ConfigEntry.class);
			if (entry == null) {
				continue;
			}

			ConfigSection entrySection = this.getConfigSection(field.getType(), false);
			if (entrySection != null) {
				String entrySectionName = entrySection.name();
				ConfigurationSection entryConfig = config.getConfigurationSection(entrySectionName);
				if (entryConfig == null) {
					entryConfig = config.createSection(entrySectionName);
				}

				Object value = this.deserializeClass(entryConfig, configClass);
				field.set(configInstance, value);
				continue;
			}

			ConfigConverter<?> converter = this.getConverter(configClass);
			Object value = converter.deserialize(config, entry);
			field.set(configInstance, value);
		}
		return configInstance;
	}

	private ConfigSection getConfigSection(Class<?> configClass, boolean throwWhenChild) {
		Objects.requireNonNull(configClass);

		ConfigSection section = configClass.getAnnotation(ConfigSection.class);
		if (section == null) {
			throw new IllegalArgumentException(configClass.getSimpleName() + " has no ConfigSection annotation.");
		}

		if (throwWhenChild && section.childSection()) {
			throw new IllegalArgumentException(configClass.getSimpleName() + " is a child section.");
		}

		return section;
	}

	@SuppressWarnings("unchecked") // TODO find a better way then using unchecked
	private ConfigConverter<Object> getConverter(Class<?> classType) {
		ConfigConverter<?> converter = this.converterList.get(classType);
		if (converter == null) {
			for (Entry<Class<?>, ConfigConverter<?>> entry : this.converterList.entrySet()) {
				if (entry.getKey().isAssignableFrom(classType)) {
					return (ConfigConverter<Object>) entry.getValue();
				}
			}
		} else {
			return (ConfigConverter<Object>) converter;
		}

		return this.defaultConverter;
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