package net.imprex.zip.storage;

import java.util.concurrent.CompletableFuture;

import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;

public interface StorageAdapter {

	CompletableFuture<Void> save(UniqueId id, Ingrim4Buffer buffer);

	CompletableFuture<Ingrim4Buffer> load(UniqueId id);

	boolean isConnected();
}
