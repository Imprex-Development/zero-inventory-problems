package net.imprex.zip.config;

import java.util.List;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSectionRoot;
import net.imprex.zip.config.storage.StorageConfig;

@SimpleSectionRoot(name = "config", version = 1)
public class GeneralConfig {

	@SimpleKey
	private Boolean checkForUpdates = true;

	@SimpleKey
	private Boolean verbose = false;

	@SimpleKey
	private Boolean metrics = true;

	@SimpleKey
	private StorageConfig storage;

	@SimpleKey
	private List<BackpackConfig> backpacks = List.of();

	public boolean isCheckForUpdates() {
		return this.checkForUpdates;
	}

	public boolean isVerbose() {
		return this.verbose;
	}

	public boolean isMetrics() {
		return this.metrics;
	}

	public StorageConfig getStorageConfig() {
		return this.storage;
	}

	public List<BackpackConfig> getBackpacks() {
		return this.backpacks;
	}
}
