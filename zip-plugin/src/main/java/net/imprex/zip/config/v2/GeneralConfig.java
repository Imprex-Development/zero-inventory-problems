package net.imprex.zip.config.v2;

@ConfigSection(name = "general")
public class GeneralConfig {

	@ConfigEntry(name = "checkForUpdates", defaultValue = "true")
	public boolean checkForUpdates;

	@ConfigEntry(name = "verbose", defaultValue = "false")
	public boolean verbose;

	@ConfigEntry(name = "locale", defaultValue = "en_US")
	public String locale;

	@ConfigEntry(name = "maxLoreCount", defaultValue = "5")
	public int maxLoreCount;
}
