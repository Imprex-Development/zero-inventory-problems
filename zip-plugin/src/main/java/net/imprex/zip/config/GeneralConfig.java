package net.imprex.zip.config;

import java.util.Map;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSectionRoot;
import net.imprex.zip.config.v3.require.SimpleInteger;

@SimpleSectionRoot(name = "config", version = 1)
public class GeneralConfig {

	@SimpleKey
	public boolean checkForUpdates = true;

	@SimpleKey
	public boolean verbose = false;

	@SimpleKey
	public String locale = "en_US";

	@SimpleInteger(min = 0, max = 64)
	@SimpleKey
	public int maxLoreCount = 10;

	@SimpleKey
	public Map<String, BackpackConfig> type = BackpackConfig.DEFAULT;
}
