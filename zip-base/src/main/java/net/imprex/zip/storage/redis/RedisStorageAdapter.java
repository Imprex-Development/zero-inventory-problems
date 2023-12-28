package net.imprex.zip.storage.redis;

import java.util.concurrent.CompletableFuture;

import net.imprex.zip.config.storage.RedisStorageConfig;
import net.imprex.zip.storage.StorageAdapter;
import net.imprex.zip.util.UniqueId;
import net.imprex.zip.util.ZipBuffer;

public class RedisStorageAdapter implements StorageAdapter {

	public RedisStorageAdapter(RedisStorageConfig config) {
	}

	private void tickQueue() {
		
	}

	@Override
	public CompletableFuture<Void> save(UniqueId id, ZipBuffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<ZipBuffer> load(UniqueId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> exist(UniqueId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> connect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> disconnect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}
}
