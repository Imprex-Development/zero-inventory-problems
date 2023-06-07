package net.imprex.zip;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;
import net.imprex.zip.util.ZIPLogger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * @author Imprex-Development
 * @see <a href="https://github.com/Imprex-Development/orebfuscator/blob/master/orebfuscator-plugin/src/main/java/net/imprex/orebfuscator/UpdateSystem.java">UpdateSystem.java</a>
 */
public class UpdateSystem {

	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-b(\\d+))?");

	private static final String API_LATEST = "https://api.github.com/repos/Imprex-Development/zero-inventory-problems/releases/latest";
	private static final long UPDATE_COOLDOWN = 1_800_000L; // 30min

	private static final String repeatString(String message, int repeat) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < repeat; i++) {
			stringBuilder.append(message);
		}
		return stringBuilder.toString();
	}

	private final Lock lock = new ReentrantLock();

	private final BackpackPlugin plugin;
	private final GeneralConfig generalConfig;
	private final MessageConfig messageConfig;

	private JsonObject releaseData;
	private long updateCooldown = -1;
	private int failedAttempts = 0;

	public UpdateSystem(BackpackPlugin plugin) {
		this.plugin = plugin;
		this.generalConfig = plugin.getBackpackConfig().general();
		this.messageConfig = plugin.getBackpackConfig().message();
		
		this.checkForUpdates();
	}

	private JsonObject getReleaseData() {
		this.lock.lock();
		try {
			long systemTime = System.currentTimeMillis();

			if (this.failedAttempts < 5) {

				if (this.releaseData != null || systemTime - this.updateCooldown > UPDATE_COOLDOWN) {
					try {
						URL url = new URL(API_LATEST);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						try (InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream())) {
							this.releaseData = JsonParser.parseReader(inputStreamReader).getAsJsonObject();
							this.updateCooldown = systemTime;
						}
					} catch (IOException e) {
						ZIPLogger.warn("Unable to fetch latest update from: " + API_LATEST);
						ZIPLogger.warn(e.toString());

						if (++this.failedAttempts == 5) {
							this.updateCooldown = systemTime;
						}
					}
				}

			} else if (systemTime - this.updateCooldown > UPDATE_COOLDOWN) {
				this.failedAttempts = 0;
				this.updateCooldown = -1;
				return this.getReleaseData();
			}

			return this.releaseData;
		} finally {
			this.lock.unlock();
		}
	}

	private String getTagName() {
		JsonObject releaseData = this.getReleaseData();
		if (releaseData != null && releaseData.has("tag_name")) {
			return releaseData.getAsJsonPrimitive("tag_name").getAsString();
		}
		return null;
	}

	private String getHtmlUrl() {
		JsonObject releaseData = this.getReleaseData();
		if (releaseData != null && releaseData.has("html_url")) {
			return releaseData.getAsJsonPrimitive("html_url").getAsString();
		}
		return null;
	}

	private boolean isDevVersion(String version) {
		Matcher matcher = VERSION_PATTERN.matcher(version);
		return matcher.find() && matcher.groupCount() == 4;
	}

	private boolean isUpdateAvailable() {
		String version = this.plugin.getDescription().getVersion();
		if (this.generalConfig.checkForUpdates && !this.isDevVersion(version)) {
			String tagName = this.getTagName();
			return tagName != null && !version.equals(tagName);
		}
		return false;
	}

	private void checkForUpdates() {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			if (this.isUpdateAvailable()) {
				String url = " " + this.getHtmlUrl() + " ";
				int lineLength = (int) Math.ceil((url.length() - 18) / 2d);
				String line = repeatString("=", lineLength);

				ZIPLogger.warn(line + " Update available " + line);
				ZIPLogger.warn(url);
				ZIPLogger.warn(repeatString("=", lineLength * 2 + 18));
			}
		});
	}

	public void checkForUpdates(Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
			if (this.isUpdateAvailable()) {
				BaseComponent[] components = new ComponentBuilder(String.format("%s%s ", this.messageConfig.get(MessageKey.ANewReleaseIsAvailable)))
						.append(this.messageConfig.getWithoutPrefix(MessageKey.ClickHere))
						.event(new ClickEvent(ClickEvent.Action.OPEN_URL, this.getHtmlUrl()))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(this.messageConfig.getWithoutPrefix(MessageKey.ClickHereToSeeTheLatestRelease)))).create();
				Bukkit.getScheduler().runTask(this.plugin, () -> {
					player.spigot().sendMessage(components);
				});
			}
		});
	}
}