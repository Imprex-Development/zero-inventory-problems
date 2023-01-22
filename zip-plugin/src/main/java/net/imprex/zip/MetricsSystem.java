package net.imprex.zip;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;

import net.imprex.zip.config.BackpackConfig;
import net.imprex.zip.util.MathUtil;

/**
 * @author Imprex-Development
 * @see https://github.com/Imprex-Development/orebfuscator/blob/master/orebfuscator-plugin/src/main/java/net/imprex/orebfuscator/MetricsSystem.java
 */
public class MetricsSystem {

	private static final NavigableMap<Integer, String> PLAYER_COUNT_GROUPS = new TreeMap<>();

	static {
		PLAYER_COUNT_GROUPS.put(8, "0-8");
		PLAYER_COUNT_GROUPS.put(16, "9-16");
		PLAYER_COUNT_GROUPS.put(32, "17-32");
		PLAYER_COUNT_GROUPS.put(64, "33-64");
		PLAYER_COUNT_GROUPS.put(128, "65-128");
		PLAYER_COUNT_GROUPS.put(Integer.MAX_VALUE, ">129");
	}

	private final Metrics metrics;

	public MetricsSystem(BackpackPlugin plugin) {
		this.metrics = new Metrics(plugin, 17495);
		this.addMemoryChart();
		this.addPlayerCountChart();
		this.addUsageCharts(plugin.getBackpackConfig());
	}

	public void addMemoryChart() {
		this.metrics.addCustomChart(new DrilldownPie("system_memory", () -> {
			final Map<String, Map<String, Integer>> result = new HashMap<>();
			final Map<String, Integer> exact = new HashMap<>();

			long memory = Runtime.getRuntime().maxMemory();
			if (memory == Long.MAX_VALUE) {
				result.put("unlimited", exact);
			} else {
				float gibiByte = Math.round(memory / 1073741824f * 100f) / 100f;
				exact.put(gibiByte + "GiB", 1);
				result.put(MathUtil.ceilToPowerOfTwo((int) gibiByte) + "GiB", exact);
			}

			return result;
		}));
	}

	public void addPlayerCountChart() {
		this.metrics.addCustomChart(new SimplePie("player_count", () -> {
			int playerCount = Bukkit.getOnlinePlayers().size();
			return PLAYER_COUNT_GROUPS.ceilingEntry(playerCount).getValue();
		}));
	}

	public void addUsageCharts(BackpackConfig config) {
		this.metrics.addCustomChart(new SimplePie("check_for_updates", () -> {
			return Boolean.toString(config.general().checkForUpdates);
		}));
	}
}