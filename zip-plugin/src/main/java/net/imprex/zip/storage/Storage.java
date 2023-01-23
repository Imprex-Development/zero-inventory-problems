package net.imprex.zip.storage;

import java.util.Set;

import net.imprex.zip.api.ZIPUniqueId;
import net.imprex.zip.config.StorageConfig;

public interface Storage {

	void connect(StorageConfig config);

	void disconnect();

	boolean isConnected();

	StorageData load(ZIPUniqueId id);

	boolean save(StorageData data);

	boolean lock(ZIPUniqueId id);

	boolean unlock(ZIPUniqueId id);

	boolean isLocked(ZIPUniqueId id);

	Set<ZIPUniqueId> getLockedIds();
}