package net.imprex.zip.storage;

import java.util.concurrent.CompletableFuture;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.config.storage.StorageConfig;
import net.imprex.zip.storage.redis.RedisStorageAdapter;
import net.imprex.zip.util.UniqueId;
import net.imprex.zip.util.ZipBuffer;

public class StorageManager {

	private StorageAdapter adapter;

	public StorageManager(BackpackPlugin plugin) {
		
	}

	public void initialize(StorageConfig config) {
		this.adapter = switch (config.getType()) {
			case "local" -> new LocalStorageAdapter(config.getLocalConfig());
			case "redis" -> new RedisStorageAdapter(config.getRedisConfig());
			default -> throw new IllegalArgumentException("Unexpected value: " + config.getType());
		};

		this.adapter.connect();
	}

	public CompletableFuture<Void> save(UniqueId id, ZipBuffer buffer) {
		return this.adapter.save(id, buffer);
	}

	public CompletableFuture<ZipBuffer> load(UniqueId id) {
		return this.adapter.load(id);
	}

	public CompletableFuture<Boolean> exist(UniqueId id) {
		return this.adapter.exist(id);
	}

	public boolean isConnected() {
		return this.adapter.isConnected();
	}
}
