package net.imprex.zip.config;

import org.bukkit.configuration.ConfigurationSection;

public class GeneralConfig {

	public boolean checkForUpdates;

	public boolean verbose;

	public String locale;

	public int maxLoreCount;

	public GeneralConfig(ConfigurationSection config) {
		if (config == null) {
			throw new IllegalArgumentException("Config section general was not found");
		}

		if (config.contains("checkForUpdates") && config.isBoolean("checkForUpdates")) {
			this.checkForUpdates = config.getBoolean("checkForUpdates");
		} else {
			throw new IllegalArgumentException("Config section general is missing errorOccured value");
		}

		if (config.contains("verbose") && config.isBoolean("verbose")) {
			this.verbose = config.getBoolean("verbose");
		} else {
			throw new IllegalArgumentException("Config section general is missing verbose value");
		}

		if (config.contains("locale")) {
			this.locale = config.getString("locale");
		} else {
			throw new IllegalArgumentException("Config section general is missing locale value");
		}

		if (config.contains("maxLoreCount") && config.isInt("maxLoreCount")) {
			this.maxLoreCount = config.getInt("maxLoreCount");
		} else {
			this.maxLoreCount = 10;
		}
	}
}