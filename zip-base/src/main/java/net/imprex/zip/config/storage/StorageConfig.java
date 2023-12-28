package net.imprex.zip.config.storage;

import net.imprex.config.SimpleComment;
import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSection;
import net.imprex.config.require.SimpleString;

@SimpleSection
public class StorageConfig {

	@SimpleKey
	@SimpleString(defaultValue = "local", regex = "(local|redis)")
	@SimpleComment("Select your database type.")
	@SimpleComment("types: local, redis")
	private String type;

	@SimpleKey
	private LocalStorageConfig local;

	@SimpleKey
	private RedisStorageConfig redis;

	public String getType() {
		return this.type;
	}

	public LocalStorageConfig getLocalConfig() {
		return this.local;
	}

	public RedisStorageConfig getRedisConfig() {
		return this.redis;
	}
}
