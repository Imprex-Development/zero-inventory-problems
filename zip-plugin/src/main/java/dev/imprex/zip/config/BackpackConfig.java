package dev.imprex.zip.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
 
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
 
import dev.imprex.zip.BackpackPlugin;
import dev.imprex.zip.common.MinecraftVersion;
import dev.imprex.zip.common.Version;
import dev.imprex.zip.common.ZIPLogger;

public class BackpackConfig {

	private static final Pattern CONFIG_FILE_PATTERN = Pattern.compile("config-(\\d+(?:\\.\\d+){0,2})\\.yml");

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
				Files.createDirectories(dataFolder);
				this.copyDefaultConfig(configPath);
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("unable to create config", e);
		}

		FileConfiguration config = this.plugin.getConfig();

		ConfigurationSection generalSection = config.getConfigurationSection("general");
		this.generalConfig = new GeneralConfig(generalSection);

		ConfigurationSection backpackTypeListSection = config.getConfigurationSection("type");
		this.typeListConfig = new BackpackTypeListConfig(backpackTypeListSection);

		this.messageConfig = new MessageConfig(this.plugin);
	}

	private void copyDefaultConfig(Path target) throws IOException, URISyntaxException {
		URI resource = BackpackPlugin.class.getResource("").toURI();
		Version current = MinecraftVersion.current();
 
		try (FileSystem fileSystem = FileSystems.newFileSystem(resource, Collections.emptyMap());
				Stream<Path> files = Files.list(fileSystem.getPath("/config"))) {
 
			TreeMap<Version, Path> configs = new TreeMap<>();
			files.forEach(file -> {
				Matcher matcher = CONFIG_FILE_PATTERN.matcher(file.getFileName().toString());
				if (matcher.matches()) {
					configs.put(Version.parse(matcher.group(1)), file);
				}
			});
 
			Map.Entry<Version, Path> entry = configs.floorEntry(current);
			if (entry == null) {
				throw new IllegalStateException(String.format(
						"no default config for minecraft version %s found (available: %s)",
						current, configs.keySet()));
			}
 
			Version configVersion = entry.getKey();
			if (configVersion.major() != current.major() || configVersion.minor() != current.minor()) {
				ZIPLogger.warn(String.format(
						"No dedicated config for minecraft version %s, falling back to config for %s",
						current, configVersion));
			}
 
			Files.copy(entry.getValue(), target);
		}
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