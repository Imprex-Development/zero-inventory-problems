package net.imprex.zip.config.storage;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSection;
import net.imprex.config.require.SimpleString;

@SimpleSection
public class LocalStorageConfig {

	@SimpleKey
	@SimpleString(defaultValue = "./plugins/zero-inventory-problems/backpacks")
	private String folderPath;

	public String getFolderPath() {
		return this.folderPath;
	}
}
