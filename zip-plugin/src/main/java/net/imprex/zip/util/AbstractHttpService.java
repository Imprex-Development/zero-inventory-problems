/**
 * @author Imprex-Development
 * @see <a href="https://github.com/Imprex-Development/orebfuscator/blob/master/orebfuscator-plugin/src/main/java/net/imprex/orebfuscator/util/AbstractHttpService.java">AbstractHttpService.java</a>
 */
package net.imprex.zip.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscribers;

import org.bukkit.plugin.PluginDescriptionFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.common.Version;

public abstract class AbstractHttpService {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(Version.class, new Version.Json())
			.create();

	public static final HttpClient HTTP = HttpClient.newHttpClient();

	protected final String userAgent;

	public AbstractHttpService(BackpackPlugin plugin) {
		PluginDescriptionFile pluginDescription = plugin.getDescription();
		this.userAgent = String.format("%s/%s", pluginDescription.getName(), pluginDescription.getVersion());
	}

	protected HttpRequest.Builder request(String url) {
		return HttpRequest.newBuilder(URI.create(url))
				.header("User-Agent", userAgent)
				.header("Accept", "application/json");
	}

	protected static <T> BodyHandler<T> json(Class<T> target) {
		return (responseInfo) -> responseInfo.statusCode() == 200
				? BodySubscribers.mapping(BodySubscribers.ofInputStream(), inputStream -> {
					try (InputStreamReader reader = new InputStreamReader(inputStream)) {
						return GSON.fromJson(new InputStreamReader(inputStream), target);
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				})
				: BodySubscribers.replacing(null);
	}
}
