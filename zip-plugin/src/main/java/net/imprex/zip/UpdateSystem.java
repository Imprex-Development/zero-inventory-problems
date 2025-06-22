/**
 * @author Imprex-Development
 * @see <a href="https://github.com/Imprex-Development/orebfuscator/blob/master/orebfuscator-plugin/src/main/java/net/imprex/orebfuscator/UpdateSystem.java">UpdateSystem.java</a>
 */
package net.imprex.zip;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.annotations.SerializedName;

import net.imprex.zip.common.MinecraftVersion;
import net.imprex.zip.common.Version;
import net.imprex.zip.common.ZIPLogger;
import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;
import net.imprex.zip.util.AbstractHttpService;
import net.imprex.zip.util.ConsoleUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class UpdateSystem extends AbstractHttpService {

	private static final Pattern DEV_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-b(?<build>\\d+))?");

	private static boolean isDevVersion(String version) {
		Matcher matcher = DEV_VERSION_PATTERN.matcher(version);
		return matcher.find() && matcher.group("build") != null;
	}

	private static final String API_URI = "https://api.modrinth.com/v2/project/zero-inventory-problems-zip-backpacks/version?loaders=%s&game_versions=%s";
	private static final String DOWNLOAD_URI = "https://modrinth.com/plugin/zero-inventory-problems-zip-backpacks/version/%s";

	private static final Duration CACHE_DURATION = Duration.ofMinutes(10L);

	private final BackpackPlugin plugin;
	private final GeneralConfig generalConfig;
	private final MessageConfig messageConfig;

	private final AtomicReference<Instant> validUntil = new AtomicReference<>();
	private final AtomicReference<CompletableFuture<Optional<ModrinthVersion>>> latestVersion = new AtomicReference<>();

	public UpdateSystem(BackpackPlugin plugin) {
		super(plugin);

		this.plugin = plugin;
		this.generalConfig = plugin.getBackpackConfig().general();
		this.messageConfig = plugin.getBackpackConfig().message();
		
		this.checkForUpdates();
	}

	private CompletableFuture<Optional<ModrinthVersion>> requestLatestVersion() {
		String installedVersion = this.plugin.getDescription().getVersion();
		if (!this.generalConfig.checkForUpdates || isDevVersion(installedVersion)) {
			ZIPLogger.debug("UpdateSystem - Update check disabled or dev version detected; skipping");
			return CompletableFuture.completedFuture(Optional.empty());
		}

		var uri = String.format(API_URI, "bukkit", MinecraftVersion.current());
		return HTTP.sendAsync(request(uri).build(), json(ModrinthVersion[].class)).thenApply(request -> {
			var version = Version.parse(installedVersion);
			var latestVersion = Arrays.stream(request.body())
					.filter(e -> Objects.equals(e.versionType, "release"))
					.filter(e -> Objects.equals(e.status, "listed"))
					.sorted(Comparator.reverseOrder())
					.findFirst();

			latestVersion.ifPresentOrElse(
					v -> ZIPLogger.debug("UpdateSystem - Fetched latest version " + v.version),
					() -> ZIPLogger.debug("UpdateSystem - Couldn't fetch latest version"));

			return latestVersion.map(v -> version.isBelow(v.version) ? v : null);
		}).exceptionally(throwable -> {
			ZIPLogger.warn("UpdateSystem - Unable to fetch latest version", throwable);
			return Optional.empty();
		});
	}

	private CompletableFuture<Optional<ModrinthVersion>> getLatestVersion() {
		Instant validUntil = this.validUntil.get();
		if (validUntil != null && validUntil.compareTo(Instant.now()) < 0 && this.validUntil.compareAndSet(validUntil, null)) {
			ZIPLogger.debug("UpdateSystem - Cleared latest cached version");
			this.latestVersion.set(null);
		}

	    CompletableFuture<Optional<ModrinthVersion>> existingFuture = this.latestVersion.get();
	    if (existingFuture != null) {
	        return existingFuture;
	    }

		CompletableFuture<Optional<ModrinthVersion>> newFuture = new CompletableFuture<>();
		if (this.latestVersion.compareAndSet(null, newFuture)) {
			ZIPLogger.debug("UpdateSystem - Starting to check for updates");
			this.requestLatestVersion().thenAccept(version -> {
				this.validUntil.set(Instant.now().plus(CACHE_DURATION));
				newFuture.complete(version);
			});
			return newFuture;
		} else {
			return this.latestVersion.get();
		}
	}

	private void ifNewerVersionAvailable(Consumer<ModrinthVersion> consumer) {
		this.getLatestVersion().thenAccept(o -> o.ifPresent(consumer));
	}

	private void checkForUpdates() {
		this.ifNewerVersionAvailable(version -> {
			String downloadUri = String.format(DOWNLOAD_URI, version.version);
			ConsoleUtil.printBox(Level.WARNING, "UPDATE AVAILABLE", "", downloadUri);
		});
	}

	public void checkForUpdates(Player player) {
		this.ifNewerVersionAvailable(version -> {
			String downloadUri = String.format(DOWNLOAD_URI, version.version);
			BaseComponent[] components = new ComponentBuilder(String.format("%s%s ", this.messageConfig.get(MessageKey.ANewReleaseIsAvailable)))
					.append(this.messageConfig.getWithoutPrefix(MessageKey.ClickHere))
					.event(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUri))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new ComponentBuilder(this.messageConfig.getWithoutPrefix(MessageKey.ClickHereToSeeTheLatestRelease)).create()))).create();
			Bukkit.getScheduler().runTask(this.plugin, () -> {
				player.spigot().sendMessage(components);
			});
		});
	}

	public static class ModrinthVersion implements Comparable<ModrinthVersion> {

		private static final Comparator<ModrinthVersion> COMPARATOR = 
				Comparator.comparing(e -> e.version, Comparator.nullsLast(Version::compareTo));

		@SerializedName("version_number")
		public Version version;

		@SerializedName("game_versions")
		public List<Version> gameVersions;

		@SerializedName("version_type")
		public String versionType;

		@SerializedName("loaders")
		public List<String> loaders;
		
		@SerializedName("status")
		public String status;

		@Override
		public int compareTo(ModrinthVersion other) {
			return COMPARATOR.compare(this, other);
		}
	}
}
