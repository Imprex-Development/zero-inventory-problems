package net.imprex.zip.migration;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.PooledByteBufAllocator;
import net.imprex.zip.Backpack;
import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.common.Ingrim4Buffer;

public class BackpackMigrator {

	private static final Map<Integer, BackpackMigration> MIGRATIONS = new HashMap<>();

	static {
		register(new BackpackMigrationV1());
	}

	private static void register(BackpackMigration migration) {
		MIGRATIONS.put(migration.sourceVersion(), migration);
	}

	public static Ingrim4Buffer migrate(BackpackPlugin plugin, Ingrim4Buffer sourceBuffer) {
		while (true) {
			int version;
			try {
				/*
				 * Forgot the version code so we need to check if we
				 * first write a byte array starting with a varInt...
				 */
				version = sourceBuffer.readVarInt();
				sourceBuffer.resetReaderIndex();

				if (version == 12) {
					version = 1;
				} else {
					version = sourceBuffer.readInt();
				}
			} catch (Exception e) {
				sourceBuffer.resetReaderIndex();
				version = sourceBuffer.readInt();
			}

			// no migration needed because were on the latest version
			if (version == Backpack.VERSION) {
				return sourceBuffer;
			}

			BackpackMigration migration = MIGRATIONS.get(version);
			if (migration == null) {
				throw new IllegalArgumentException("Missing backpack migration step for source version '" + version + "'");
			}

			Ingrim4Buffer targetBuffer = new Ingrim4Buffer(PooledByteBufAllocator.DEFAULT.buffer());
			targetBuffer.writeInt(migration.targetVersion()); // write version
			migration.migrate(plugin, sourceBuffer, targetBuffer);

			sourceBuffer.release();
			sourceBuffer = targetBuffer;
		}
	}
}