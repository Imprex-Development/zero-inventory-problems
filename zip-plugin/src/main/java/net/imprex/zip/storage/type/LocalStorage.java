package net.imprex.zip.storage.type;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.imprex.zip.api.ZIPUniqueId;
import net.imprex.zip.config.StorageConfig;
import net.imprex.zip.storage.Storage;
import net.imprex.zip.storage.StorageData;

public class LocalStorage implements Storage {

	private Path dataFolder;

	private AtomicBoolean connected = new AtomicBoolean(false);

	private Set<ZIPUniqueId> locked = new LinkedHashSet<>();

	@Override
	public void connect(StorageConfig config) {
		if (this.connected.compareAndSet(false, true)) {
			
		} else {
			throw new IllegalStateException("Local storage is already connected");
		}
	}

	@Override
	public void disconnect() {
		if (this.connected.compareAndSet(true, false)) {
			
		} else {
			throw new IllegalStateException("Local storage is already disconnected");
		}
	}

	@Override
	public boolean isConnected() {
		return this.connected.get();
	}

	@Override
	public StorageData load(ZIPUniqueId id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean save(StorageData data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean lock(ZIPUniqueId id) {
		synchronized (this.locked) {
			return this.locked.add(id);
		}
	}

	@Override
	public boolean unlock(ZIPUniqueId id) {
		synchronized (this.locked) {
			return this.locked.remove(id);
		}
	}

	@Override
	public boolean isLocked(ZIPUniqueId id) {
		return this.locked.contains(id);
	}

	@Override
	public Set<ZIPUniqueId> getLockedIds() {
		return Collections.unmodifiableSet(this.locked);
	}
}