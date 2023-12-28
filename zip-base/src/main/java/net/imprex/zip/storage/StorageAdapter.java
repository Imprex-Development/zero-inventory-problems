package net.imprex.zip.storage;

import java.util.concurrent.CompletableFuture;

import net.imprex.zip.util.UniqueId;
import net.imprex.zip.util.ZipBuffer;

public interface StorageAdapter {

	CompletableFuture<Void> save(UniqueId id, ZipBuffer buffer);

	CompletableFuture<ZipBuffer> load(UniqueId id);

	CompletableFuture<Boolean> exist(UniqueId id);

	CompletableFuture<Boolean> connect();

	CompletableFuture<Void> disconnect();

	boolean isConnected();
}
