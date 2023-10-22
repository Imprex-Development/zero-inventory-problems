package net.imprex.zip.config.translation;

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

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.util.ZIPLogger;
import net.md_5.bungee.api.ChatColor;

public class TranslationLoader {

	private final BackpackPlugin plugin;

	private final Map<Message, String> messages = new HashMap<>();

	private Path folderPath;

	public TranslationLoader(BackpackPlugin plugin) {
		this.plugin = plugin;

		this.folderPath = plugin.getDataFolder().toPath().resolve("lang");
	}

	public void copyLocaleWhenNotExist() {
		try {
			URI resource = BackpackPlugin.class.getResource("").toURI();

			try (FileSystem fileSystem = FileSystems.newFileSystem(
					resource,
					Collections.emptyMap())) {
				Files.createDirectories(this.folderPath);
				
				Path langPath = fileSystem.getPath("/lang");
				Files.walkFileTree(langPath, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Path filePath = TranslationLoader.this.folderPath.resolve(langPath.relativize(file).toString());
						if (Files.notExists(filePath)) {
							Files.copy(file, TranslationLoader.this.folderPath.resolve(langPath.relativize(file).toString()));
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
		Path localeFile = this.folderPath.resolve(locale + ".yml");
		if (Files.notExists(localeFile)) {
			ZIPLogger.warn("No config path for locale " + this.plugin.getBackpackConfig().locale + ".yml found in lang folder");
			return;
		}

		YamlConfiguration config = new YamlConfiguration();
		try (BufferedReader reader = Files.newBufferedReader(localeFile)) {
			config.load(reader);
		} catch (Exception e) {
			ZIPLogger.error("Unable to read locale config file " + locale + ".yml", e);
		}

		for (String key : config.getKeys(false)) {
			Message messageKey = Message.findByKey(key);
			if (messageKey != null) {
				this.messages.put(messageKey, ChatColor.translateAlternateColorCodes('&', config.getString(key)));
			} else {
				ZIPLogger.warn(String.format("Unable to find a vaild message key for \"%s\" in language file \"%s\"", key, locale));
			}
		}

		for (Message messageKey : Message.values()) {
			if (this.messages.containsKey(messageKey)) {
				continue;
			}

			ZIPLogger.warn(String.format("Unable to find a message key for \"%s\" in language file \"%s\"", messageKey.getKey(), locale));
		}
	}

	public String get(Message key, Object... args) {
		return this.getWithoutPrefix(Message.Prefix) + this.getWithoutPrefix(key, args);
	}

	public String getWithoutPrefix(Message key, Object... args) {
		String message = this.messages.get(key);
		if (message == null) {
			message = key.getDefaultMessage();
		}

		return MessageFormat.format(message, args);
	}

	public void send(CommandSender sender, Message key, Object... args) {
		sender.sendMessage(this.get(key, args));
	}
}
