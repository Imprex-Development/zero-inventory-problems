package net.imprex.zip;

import java.nio.file.Path;

import net.imprex.config.SimpleConfig;
import net.imprex.zip.api.ZIPService;
import net.imprex.zip.config.GeneralConfig;
import net.imprex.zip.storage.StorageManager;
import net.imprex.zip.translation.Translation;

public abstract class BackpackPlugin implements ZIPService {

	private Path dataFolder;

	private SimpleConfig<GeneralConfig> config;
	private Translation translation;

	private StorageManager storageManager;

	private BackpackRegistry backpackRegistry;
	private BackpackHandler backpackHandler;

	private UpdateSystem updateSystem;

	public void load(Path dataFolder) {
		this.dataFolder = dataFolder;

		this.config = new SimpleConfig<>(this.dataFolder, GeneralConfig.class);
		this.translation = new Translation(this);

		this.storageManager = new StorageManager(this);

		this.backpackRegistry = new BackpackRegistry(this);
		this.backpackHandler = new BackpackHandler(this);
	}

	public void enable() throws Exception {
		NmsInstance.initialize();

		this.config.deserialize(true);
		this.translation.initialize();
		this.storageManager.initialize(this.getZipConfig().getStorageConfig());
		this.backpackRegistry.register();

		this.updateSystem = new UpdateSystem(this);
		new MetricsSystem(this);
	}

	public void disable() {
		if (this.backpackHandler != null) {
			this.backpackHandler.disable();
		}
		if (this.backpackRegistry != null) {
			this.backpackRegistry.unregister();
		}
	}

	public abstract void runTask(Runnable task);

	public abstract void runTaskAsync(Runnable task);

	public abstract void runTaskTimer(int delay, Runnable task);

	public abstract void runTaskTimerAsync(int delay, Runnable task);

	public GeneralConfig getZipConfig() {
		return this.config.getOrDeserializeConfig();
	}

	public Translation getTranslation() {
		return this.translation;
	}

	public UpdateSystem getUpdateSystem() {
		return this.updateSystem;
	}

	public StorageManager getStorageManager() {
		return this.storageManager;
	}

	@Override
	public BackpackHandler getBackpackHandler() {
		return this.backpackHandler;
	}

	@Override
	public BackpackRegistry getBackpackRegistry() {
		return this.backpackRegistry;
	}
}