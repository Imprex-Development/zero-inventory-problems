package net.imprex.zip.config;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;

public class GeneralConfig {

	public boolean checkForUpdates;

	public boolean verbose;

	public String locale;
	public Locale dateLocale;
	public ZoneId dateZoneId;
	public String dateFormat;

	public int maxLoreCount;

	public int historySize;

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

			try {
				this.dateLocale = Locale.forLanguageTag(this.locale);
			} catch (Exception e) {
				e.printStackTrace();
				this.dateLocale = Locale.getDefault();
			}
		} else {
			throw new IllegalArgumentException("Config section general is missing locale value");
		}

		if (config.contains("maxLoreCount") && config.isInt("maxLoreCount")) {
			this.maxLoreCount = config.getInt("maxLoreCount");
		} else {
			this.maxLoreCount = 10;
		}

		if (config.contains("historySize") && config.isInt("historySize")) {
			this.historySize = config.getInt("historySize");
		} else {
			this.historySize = 5;
		}

		try {
			this.dateZoneId = ZoneId.of(config.getString("dateZoneId"));
		} catch (Exception e) {
			// TODO log missing config value
			e.printStackTrace();
			this.dateZoneId = ZoneId.systemDefault();
		}

		try {
			this.dateFormat = config.getString("dateFormat");
			DateTimeFormatter.ofPattern(this.dateFormat, this.dateLocale);
		} catch (Exception e) {
			// TODO log missing config value
			e.printStackTrace();
			this.dateFormat = "MM.dd.yyyy HH:mm";
		}
	}
}