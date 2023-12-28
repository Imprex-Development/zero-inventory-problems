package net.imprex.zip.translation;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.mojang.brigadier.context.CommandContext;

import de.ngloader.twitchinteractions.util.Chat;
import net.imprex.config.SimpleConfig;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.util.ZipLogger;

public class Translation {

	private final Map<Message, String> messages = new HashMap<>();

	private final SimpleConfig<TranslationConfig> config;

	public Translation(BackpackPlugin plugin) {
		this.config = new SimpleConfig<>(plugin.getDataFolder().toPath(), TranslationConfig.class);
	}

	public void initialize() {
		try {
			TranslationConfig translationConfig = this.config.deserialize(true);
			Map<String, String> replacement = translationConfig.getReplacement();

			for (Map.Entry<String, String> entry : replacement.entrySet()) {
				String content = entry.getValue();
				for (Map.Entry<String, String> entryReplacement : replacement.entrySet()) {
					if (entry.getKey().equals(entryReplacement.getKey())) {
						continue;
					}

					content = content.replace(entryReplacement.getKey(), entryReplacement.getValue());
				}
				entry.setValue(content);
			}
			
			for (Map.Entry<String, String> entry : translationConfig.getMessage().entrySet()) {
				String key = entry.getKey();
				Message message = Message.findByKey(key);

				if (message != null) {
					String content = entry.getValue();
					for (Map.Entry<String, String> entryReplacement : replacement.entrySet()) {
						content = content.replace(entryReplacement.getKey(), entryReplacement.getValue());
					}
					
					this.messages.put(message, ChatColor.translateAlternateColorCodes('&', content));
				} else {
					ZipLogger.warn(String.format("Unable to find a vaild message key for \"%s\"", key));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public String getMessage(Message key) {
		return this.messages.getOrDefault(key, key.getDefaultMessage());
	}

	public void send(CommandContext<CommandSender> context, Message key, Object... args) {
		this.send(context.getSource(), key, args);
	}

	public void send(CommandSender sender, Message key, Object... args) {
		Chat.send(sender, this.getMessage(key), args);
	}
}
