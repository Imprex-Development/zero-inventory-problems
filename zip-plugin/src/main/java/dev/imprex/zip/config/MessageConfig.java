package dev.imprex.zip.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.imprex.zip.BackpackPlugin;
import dev.imprex.zip.common.ZIPLogger;
import net.md_5.bungee.api.ChatColor;

public class MessageConfig {

	private final BackpackPlugin plugin;

	private final Map<MessageKey, String> messages = new HashMap<>();

	private String locale;
	private Path localeFolder;
	private Path localeFile;

	public MessageConfig(BackpackPlugin plugin) {
		this.plugin = plugin;

		this.locale = plugin.getBackpackConfig().general().locale;
		this.localeFolder = plugin.getDataFolder().toPath().resolve("lang");
		this.localeFile = this.localeFolder.resolve(this.locale + ".yml");

		this.copyLocaleWhenNotExist();
		this.loadLocale(plugin.getBackpackConfig().general().locale);
	}

	public void copyLocaleWhenNotExist() {
		try {
			URI resource = BackpackPlugin.class.getResource("").toURI();

			try (FileSystem fileSystem = FileSystems.newFileSystem(
					resource,
					Collections.emptyMap())) {
				Files.createDirectories(this.localeFolder);
				
				Path langPath = fileSystem.getPath("/lang");
				Files.walkFileTree(langPath, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Path filePath = MessageConfig.this.localeFolder.resolve(langPath.relativize(file).toString());
						if (Files.notExists(filePath)) {
							Files.copy(file, MessageConfig.this.localeFolder.resolve(langPath.relativize(file).toString()));
						}
						return FileVisitResult.CONTINUE;
					}
				});
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("unable to copy locale configs", e);
		}
	}

	public void loadLocale(String locale) {
		if (Files.notExists(this.localeFile)) {
			ZIPLogger.warn("No config path for locale " + this.plugin.getBackpackConfig().general().locale + ".yml found in lang folder");
			return;
		}

		YamlConfiguration config = new YamlConfiguration();
		try (BufferedReader reader = Files.newBufferedReader(this.localeFile)) {
			config.load(reader);
		} catch (Exception e) {
			ZIPLogger.error("Unable to read locale config file " + this.locale + ".yml", e);
		}

		for (String key : config.getKeys(false)) {
			MessageKey messageKey = MessageKey.findByKey(key);
			if (messageKey != null) {
				this.messages.put(messageKey, ChatColor.translateAlternateColorCodes('&', config.getString(key)));
			} else {
				ZIPLogger.warn(String.format("Unable to find a vaild message key for \"%s\" in language file \"%s\"", key, this.locale));
			}
		}

		for (MessageKey messageKey : MessageKey.values()) {
			if (this.messages.containsKey(messageKey)) {
				continue;
			}

			ZIPLogger.warn(String.format("Unable to find a message key for \"%s\" in language file \"%s\"", messageKey.getKey(), this.locale));
		}
	}

	public String get(MessageKey key, Object... args) {
		return this.getWithoutPrefix(MessageKey.Prefix) + this.getWithoutPrefix(key, args);
	}

	public String getWithoutPrefix(MessageKey key, Object... args) {
		String message = this.messages.get(key);
		if (message == null) {
			message = key.getDefaultMessage();
		}

		return MessageFormat.format(message, args);
	}

	public void send(CommandSender sender, MessageKey key, Object... args) {
		sender.sendMessage(this.get(key, args));
	}
}
