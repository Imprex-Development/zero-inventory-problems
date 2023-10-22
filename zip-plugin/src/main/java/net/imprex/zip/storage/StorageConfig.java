package net.imprex.zip.storage;

import net.imprex.zip.config.v3.SimpleComment;
import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSection;
import net.imprex.zip.config.v3.require.SimpleString;

@SimpleSection
public class StorageConfig {

	@SimpleComment("Select your database type.")
	@SimpleComment("types: local")
	@SimpleString(regex = "(local)", defaultValue = "local")
	@SimpleKey
	public String databaseType;
}
