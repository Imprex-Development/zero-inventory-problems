package net.imprex.zip.migration;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.common.Ingrim4Buffer;

public interface BackpackMigration {

	int sourceVersion();

	default int targetVersion() {
		return this.sourceVersion() + 1;
	}

	Ingrim4Buffer migrate(BackpackPlugin plugin, Ingrim4Buffer sourceBuffer, Ingrim4Buffer targetBuffer);
}
