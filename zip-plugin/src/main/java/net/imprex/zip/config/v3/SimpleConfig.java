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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;

public class SimpleConfig<Config> {

	private static final String VERSION_FIELD = "version";

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

	private List<String> getComments(Field field) {
		SimpleCommentList commentList = field.getAnnotation(SimpleCommentList.class);
		if (commentList != null) {
			return Stream.of(commentList.value()).map(SimpleComment::value).toList();
		}

		SimpleComment comment = field.getAnnotation(SimpleComment.class);
		if (comment != null) {
			return List.of(comment.value());
		}

		return Collections.emptyList();
	}

	public void serialize() throws Exception {
		Objects.requireNonNull(this.instance, "config wasn't loaded before!");

		YamlConfiguration config = new YamlConfiguration();
		config.set(VERSION_FIELD, this.rootSection.version());
		this.serialize(config, this.instance);

		Path configFilePath = this.getConfigPath();
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
					throw new IllegalArgumentException(String.format("Section name '%s' in '%s' is not allowed to use.",
							VERSION_FIELD,
							instance.getClass().getSimpleName()));
				}

				ConfigurationSection newSection = config.getConfigurationSection(newSectionName);
				if (newSection == null) {
					newSection = config.createSection(newSectionName);
				}

				Object newSectionInstance = field.get(instance);
				this.serialize(newSection, newSectionInstance);

				List<String> comments = this.getComments(field);
				if (comments.size() != 0) {
					config.setInlineComments(newSectionName, comments);
				}
				continue;
			}

			SimpleKey fieldEntry = field.getAnnotation(SimpleKey.class);
			if (fieldEntry == null) {
				// TODO log that the no entry exists for current field
				continue;
			}

			// Check if version field will be overwritten in root section
			String fieldName = fieldEntry.value();
			if (fieldName.equalsIgnoreCase(VERSION_FIELD) && config.getRoot().equals(config)) {
				throw new IllegalArgumentException(String.format("Field name '%s' in '%s' is not allowed to use.",
						VERSION_FIELD,
						instance.getClass().getSimpleName()));
			}

			Object value = field.get(instance);
			if (value != null) {
				SimpleTranslator<Object, Annotation> translator = (SimpleTranslator<Object, Annotation>) SimpleTranslatorRegistry.getTranslator(fieldType);
				translator.serialize(config, fieldEntry, value);
			} else {
				config.set(fieldName, ""); // TODO test if value is set to null when serialized!
			}

			List<String> comments = this.getComments(field);
			if (comments.size() != 0) {
				config.setInlineComments(fieldName, comments);
			}
		}
	}

	public Config deserialize() throws Exception {
		Path configFilePath = this.getConfigPath();

		YamlConfiguration config = new YamlConfiguration();
		try (InputStream inputStream = Files.newInputStream(configFilePath, StandardOpenOption.CREATE);
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charsets.UTF_8)) {
			config.load(inputStreamReader);
		}

		int version = config.getInt(VERSION_FIELD, -1);
		if (version == -1) {
			throw new IllegalStateException("Unable to detect config version!");
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
//			field.get

			Class<?> fieldType = field.getType();

			String newSectionName = this.getSectionName(fieldType);
			if (newSectionName != null) {
				ConfigurationSection newSection = config.getConfigurationSection(newSectionName);
				if (newSection == null) {
					newSection = config.createSection(newSectionName);
				}

				this.deserialize(newSection, fieldType);
				continue;
			}
			
			SimpleKey fieldEntry = field.getAnnotation(SimpleKey.class);
			if (fieldEntry == null) {
				// TODO log that the no entry exists for current field
				continue;
			}

			SimpleTranslator<Object, Require> translator = (SimpleTranslator<Object, Require>) SimpleTranslatorRegistry.getTranslator(fieldType);
			Object value = translator.deserialize(config, fieldEntry);
			if (value != null) {
				Class<? extends Object> requires = translator.requires();
				if (requires != null) {
					Require requiresAnnotation = field.getAnnotation((Class<Require>) requires);
					Object result = translator.requirement(fieldEntry, value, requiresAnnotation);

					if (!result.equals(value)) {
						System.out.println("Invalid input! TODO"); // TODO log that.
					}
					value = result;
				}
				
				field.set(instance, value);
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