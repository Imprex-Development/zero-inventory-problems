package net.imprex.zip.storage;

import java.util.concurrent.CompletableFuture;

import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;

public class Storage {

	private StorageAdapter adapter;

	public void initialize(StorageConfig config) {
		
	}

	public CompletableFuture<Void> save(UniqueId id, Ingrim4Buffer buffer) {
		return this.adapter.save(id, buffer);
	}

	public CompletableFuture<Ingrim4Buffer> load(UniqueId id) {
		return this.adapter.load(id);
	}

	public boolean isConnected() {
		return this.adapter.isConnected();
	}
}
