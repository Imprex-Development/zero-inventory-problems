package net.imprex.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;

import net.imprex.zip.util.ZipLogger;

public class SimpleConfig<Config> {

	public static <T> T getOrDefault(T object, T defaultValue) {
		return object != null ? object : defaultValue;
	}

	public static <T> T getOrDefault(T object, T defaultValue, Predicate<T> predicate) {
		return object != null ? predicate.test(object) ? object : defaultValue : defaultValue;
	}

	static final String VERSION_FIELD = "version";

	private final Path dataFolder;
	private final Class<? extends Config> configClass;

	private final SimpleSectionRoot rootSection;

	private Config instance;

	public SimpleConfig(Plugin plugin, Class<? extends Config> configClass) {
		this(plugin.getDataFolder().toPath(), configClass);
	}

	public SimpleConfig(Path dataFolder, Class<? extends Config> configClass) {
		Objects.requireNonNull(dataFolder, "dataFolder can't be null!");
		Objects.requireNonNull(configClass, "configClass can't be null!");

		this.dataFolder = dataFolder;
		this.configClass = configClass;

		this.rootSection = configClass.getAnnotation(SimpleSectionRoot.class);
		Objects.requireNonNull(this.rootSection, configClass.getSimpleName() + " has no SimpleSectionRoot annotation!");
	}

	private Path getConfigPath() throws IOException {
		Path folderPath = this.dataFolder.resolve(this.rootSection.directory());
		Files.createDirectories(folderPath);

		String fileName = this.rootSection.name();
		if (!fileName.endsWith(this.rootSection.fileSuffix())) {
			fileName = fileName + this.rootSection.fileSuffix();
		}

		Path filePath = folderPath.resolve(fileName);
		return filePath;
	}

	private void setComments(ConfigurationSection config, SimpleField<?> field, String fieldName) {
		List<String> comments = field.getComments(false);
		List<String> inlineComments = field.getComments(true);

		if (comments.size() != 0) {
			config.setComments(fieldName, comments);
		}

		if (inlineComments.size() != 0) {
			config.setInlineComments(fieldName, inlineComments);
		}
	}

	public void serialize() throws Exception {
		Objects.requireNonNull(this.instance, "config wasn't loaded before!");

		YamlConfiguration config = new YamlConfiguration();
		config.set(VERSION_FIELD, this.rootSection.version());
		config.setComments(VERSION_FIELD, SimpleField.getComments(false, this.configClass));
		config.setInlineComments(VERSION_FIELD, SimpleField.getComments(true, this.configClass));
		this.serialize(config, this.instance);

		Path configFilePath = this.getConfigPath();
		if (Files.notExists(configFilePath)) {
			Files.createFile(configFilePath);
		}
		
		try (OutputStream outputStream = Files.newOutputStream(configFilePath, StandardOpenOption.CREATE);
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, Charsets.UTF_8)) {
			String configSaved = config.saveToString();
			outputStreamWriter.write(configSaved);
		}
	}

	@SuppressWarnings("unchecked")
	private <Type, Require extends Annotation> void serialize(ConfigurationSection config, Object instance) throws Exception {
		for (SimpleField<?> field : SimpleField.getFields(instance.getClass())) {
			SimpleTranslatorKey translatorKey = SimpleTranslatorKey.key(field);
			if (translatorKey == null) {
				ZipLogger.info("No SimpleKey annotation found in section " + config.getName() + " (Class: " + this.configClass.getSimpleName() + ")");
				continue;
			}

			if (field.isSection()) {
				String newSectionName = translatorKey.name();
				// Check if version field will be overwritten in root section
				if (newSectionName.equalsIgnoreCase(VERSION_FIELD) && config.getRoot().equals(config)) {
					throw new IllegalArgumentException(String.format("Section name '%s' in '%s' is not allowed to use in root section.",
							VERSION_FIELD,
							instance.getClass().getSimpleName()));
				}

				ConfigurationSection newSection = config.getConfigurationSection(newSectionName);
				if (newSection == null) {
					newSection = config.createSection(newSectionName);
				}


				// Create new config section when current value is null
				Object newSectionInstance = field.getField(instance);
				if (newSectionInstance == null && field.isChildSection()) {
					try {
						Constructor<?> constructor = field.getType().getConstructor();
						newSectionInstance = constructor.newInstance();
					} catch (NoSuchMethodException e) {
						ZipLogger.error("Unable to create config section for " + newSectionName + " in " + this.configClass.getSimpleName(), e);
					}
				}

				if (newSectionInstance != null) {
					this.serialize(newSection, newSectionInstance);
				}

				this.setComments(config, field, newSectionName);
				continue;
			}

			// Check if version field will be overwritten in root section
			if (translatorKey.isVersionInRoot(config)) {
				throw new IllegalArgumentException(String.format("Field name '%s' in '%s' is not allowed to use in root section.",
						VERSION_FIELD,
						instance.getClass().getSimpleName()));
			}

			SimpleTranslator<Type, Require> translator = SimpleTranslatorRegistry.getTranslator(field);
			Require requireAnnotation = field.getRequire(translator);

			Type initialValue = (Type) field.getField(instance);
			Type defaultValue = (Type) translator.defaultValue(translatorKey, initialValue, requireAnnotation);

			translator.serialize(config, translatorKey, defaultValue, requireAnnotation);
			this.setComments(config, field, translatorKey.name());
		}
	}

	public Config deserialize() throws Exception {
		return this.deserialize(false);
	}

	public Config deserialize(boolean storeMissingValue) throws Exception {
		Path configFilePath = this.getConfigPath();

		if (Files.notExists(configFilePath)) {
			this.instance = this.deserialize(new YamlConfiguration(), this.configClass);
			this.serialize();
		}

		YamlConfiguration config = new YamlConfiguration();
		try (InputStream inputStream = Files.newInputStream(configFilePath, StandardOpenOption.CREATE);
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)) {
			config.load(inputStreamReader);
		}

		int version = config.getInt(VERSION_FIELD, -1);
		if (version == -1) {
			throw new IllegalStateException("Unable to detect config version!");
		} else if (version < this.rootSection.version()) {
			throw new IllegalStateException("Config version is higher then the currently one! (config: " + version + " current:" + this.rootSection.version() + ")");
		}
		// TODO handle migration

		this.instance = this.deserialize(config, this.configClass);
		
		// TODO storeMissingValue
		return this.instance;
	}

	@SuppressWarnings("unchecked")
	private <Type, Require extends Annotation> Type deserialize(ConfigurationSection config, Class<? extends Type> configClass) throws Exception {
		Constructor<? extends Type> constructor = configClass.getConstructor();
		Objects.requireNonNull(constructor, configClass.getSimpleName() + " no empty constructor was found!");

		Type instance = constructor.newInstance();

		for (SimpleField<?> field : SimpleField.getFields(configClass)) {
			SimpleTranslatorKey translatorKey = SimpleTranslatorKey.key(field);
			if (translatorKey == null) {
				ZipLogger.info("No SimpleKey annotation found in section " + config.getName() + " (Class: " + this.configClass.getSimpleName() + ")");
				continue;
			}


			if (field.isSection()) {
				String newSectionName = translatorKey.name();
				ConfigurationSection newSection = config.getConfigurationSection(newSectionName);
				if (newSection == null) {
					newSection = config.createSection(newSectionName);
				}

				Object newSectionInstance = this.deserialize(newSection, field.getType());
				field.setField(instance, newSectionInstance);
				continue;
			}

			SimpleTranslator<Type, Require> translator = SimpleTranslatorRegistry.getTranslator(field);
			Require requireAnnotation = field.getRequire(translator);

			Type initialValue = (Type) field.getField(instance);
			Type defaultValue = translator.defaultValue(translatorKey, initialValue, requireAnnotation);
			Type value = translator.deserialize(config, translatorKey, defaultValue, requireAnnotation);

			if (value != null) {
				if (requireAnnotation != null) {
					Object result = translator.requirement(translatorKey, value, requireAnnotation);
					if (result != null && !result.equals(value)) {
						ZipLogger.warn("Unsing default value (" + result + ") for key \"" + translatorKey.name() + "\" in section " + config.getName());
					}
					field.setField(instance, result);
					continue;
				}

				if (field.getType().isAssignableFrom(value.getClass())) {
					field.setField(instance, value);
					continue;
				}
			}

			ZipLogger.warn("Unsing default value (" + defaultValue + ") for key \"" + translatorKey.name() + "\" in section " + config.getName());
			field.setField(instance, defaultValue);
		}

		return instance;
	}

	public Config getOrDeserializeConfig() {
		if (this.instance == null) {
			try {
				this.deserialize();
			} catch (Exception e) {
				ZipLogger.error("Unable to deserialize config class of " + this.configClass.getSimpleName(), e);
			}
		}
		return this.instance;
	}
}