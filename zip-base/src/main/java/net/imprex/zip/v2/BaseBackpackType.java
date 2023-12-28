package net.imprex.zip.v2;

import net.imprex.zip.config.BackpackConfig;

public class BaseBackpackType<TItem> {

	private final BackpackConfig config;

	public BaseBackpackType(BackpackConfig config) {
		this.config = config;
	}
}
