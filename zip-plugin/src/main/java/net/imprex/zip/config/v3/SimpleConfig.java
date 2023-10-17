package net.imprex.zip.config.v3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;

import net.imprex.zip.util.ZIPLogger;

public class SimpleConfig<Config> {

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

	private String getSectionName(Class<?> configClass) {
		SimpleSection section = configClass.getAnnotation(SimpleSection.class);
		if (section != null) {
			return section.name();
		}

		SimpleSectionRoot rootSection = configClass.getAnnotation(SimpleSectionRoot.class);
		if (rootSection != null) {
			return rootSection.name();
		}

		return null;
	}

	private void setComments(ConfigurationSection config, Field field, String fieldName) {
		List<String> comments = new ArrayList<>();
		List<String> inlineComments = new ArrayList<>();

		SimpleCommentList commentList = field.getAnnotation(SimpleCommentList.class);
		if (commentList != null) {
			for (SimpleComment comment : commentList.value()) {
				(comment.inline() ? inlineComments : comments).add(comment.value());
			}
		}

		SimpleComment comment = field.getAnnotation(SimpleComment.class);
		if (comment != null) {
			(comment.inline() ? inlineComments : comments).add(comment.value());
		}

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
	private void serialize(ConfigurationSection config, Object instance) throws Exception {
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (field.getAnnotation(SimpleUnused.class) != null) {
				continue;
			}

			Class<?> fieldType = field.getType();

			String newSectionName = this.getSectionName(fieldType);
			if (newSectionName != null) {
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
				Object newSectionInstance = field.get(instance);
				if (newSectionInstance == null && fieldType.getAnnotation(SimpleSection.class) != null) {
					try {
						Constructor<?> constructor = fieldType.getConstructor();
						newSectionInstance = constructor.newInstance();
					} catch (NoSuchMethodException e) {
						ZIPLogger.error("Unable to create config section for " + newSectionName + " in " + this.configClass.getSimpleName(), e);
					}
				}

				if (newSectionInstance != null) {
					this.serialize(newSection, newSectionInstance);
				}

				this.setComments(config, field, newSectionName);
				continue;
			}

			SimpleTranslatorKey translatorKey = SimpleTranslatorKey.key(field);
			if (translatorKey == null) {
				// TODO log that the no entry exists for current field
				continue;
			}

			// Check if version field will be overwritten in root section
			if (translatorKey.isVersionInRoot(config)) {
				throw new IllegalArgumentException(String.format("Field name '%s' in '%s' is not allowed to use in root section.",
						VERSION_FIELD,
						instance.getClass().getSimpleName()));
			}

			Object value = field.get(instance);
			if (value != null) {
				SimpleTranslator<Object, Annotation> translator = (SimpleTranslator<Object, Annotation>) SimpleTranslatorRegistry.getTranslator(fieldType);
				translator.serialize(config, translatorKey, value);
			} else {
				config.set(translatorKey.name(), ""); // TODO test if value is set to null when serialized!
			}

			this.setComments(config, field, translatorKey.name());
		}
	}

	public Config deserialize() throws Exception {
		Path configFilePath = this.getConfigPath();

		if (Files.notExists(configFilePath)) {
			this.instance = this.deserialize(new YamlConfiguration(), this.configClass);
//			return this.instance;
			System.out.println(this.instance);
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
			throw new IllegalStateException("Config version is " + version + " and is higher then the currently one (" + this.rootSection.version() + ")");
		}
		// TODO handle migration

		this.instance = this.deserialize(config, this.configClass);
		return this.instance;
	}

	@SuppressWarnings("unchecked")
	private <Type, Require extends Annotation> Type deserialize(ConfigurationSection config, Class<? extends Type> configClass) throws Exception {
		Constructor<? extends Type> constructor = configClass.getConstructor();
		Objects.requireNonNull(constructor, configClass.getSimpleName() + " no empty constructor was found!");

		Type instance = constructor.newInstance();

		for (Field field : configClass.getDeclaredFields()) {
			if (field.getAnnotation(SimpleUnused.class) != null) {
				continue;
			}

			Class<?> fieldType = field.getType();

			String newSectionName = this.getSectionName(fieldType);
			if (newSectionName != null) {
				ConfigurationSection newSection = config.getConfigurationSection(newSectionName);
				if (newSection == null) {
					newSection = config.createSection(newSectionName);
				}

				Object newSectionInstance = this.deserialize(newSection, fieldType);
				field.set(instance, newSectionInstance);
				continue;
			}
			System.out.println(configClass.getSimpleName() + " " + field.getName());

			SimpleTranslatorKey translatorKey = SimpleTranslatorKey.key(field);
			if (translatorKey == null) {
				ZIPLogger.info("No SimpleKey annotation found in section " + config.getName() + " (Class: " + configClass.getSimpleName() + ")");
				continue;
			}

			SimpleTranslator<Object, Require> translator = (SimpleTranslator<Object, Require>) SimpleTranslatorRegistry.getTranslator(fieldType);
			Object value = translator.deserialize(config, translatorKey);

			Class<? extends Object> requires = translator.requires();
			Require requiresAnnotation = null;
			if (requires != null) {
				requiresAnnotation = field.getAnnotation((Class<Require>) requires);
			}

			boolean setDefaultValue = value == null;
			if (value != null) {
				if (requiresAnnotation != null) {
					Object result = translator.requirement(translatorKey, value, requiresAnnotation);
					if (result != null && !result.equals(value)) {
						ZIPLogger.warn("Unsing default value (" + result + ") for key \"" + translatorKey.name() + "\" in section " + config.getName());
					}
					field.set(instance, result);
					continue;
				}

				if (field.getGenericType().getTypeName().equals(value.getClass().getTypeName())) {
					field.set(instance, value);
					continue;
				} else {
					setDefaultValue = true;
				}
			}

			if (setDefaultValue && requiresAnnotation != null) {
				field.set(instance, translator.defaultValue(translatorKey, requiresAnnotation));
			}
		}

		return instance;
	}

	public Config getOrDeserializeConfig() throws Exception {
		if (this.instance == null) {
			this.deserialize();
		}
		return this.instance;
	}
}