package net.imprex.zip.migration;

import org.bukkit.inventory.ItemStack;

import net.imprex.zip.BackpackPlugin;
import net.imprex.zip.NmsInstance;
import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;

public class BackpackMigrationV1 implements BackpackMigration {

	private UniqueId id;

	private String typeRaw;

	private ItemStack[] content;

	@Override
	public int sourceVersion() {
		return 1;
	}

	@Override
	public int targetVersion() {
		return 100;
	}

	@Override
	public Ingrim4Buffer migrate(BackpackPlugin plugin, Ingrim4Buffer sourceBuffer, Ingrim4Buffer targetBuffer) {
		this.read(plugin, sourceBuffer);

		targetBuffer.writeByteArray(this.id.toByteArray());
		targetBuffer.writeString(this.typeRaw);
		targetBuffer.writeByteArray(NmsInstance.itemstackArrayToBinary(this.content));

		// version 2
		// write history size
		targetBuffer.writeInt(0);

		return targetBuffer;
	}

	public void read(BackpackPlugin plugin, Ingrim4Buffer buffer) {
		/*
		 * Load backpack id from buffer but don't use it! Just for later migration to
		 * SQL
		 */
		this.id = UniqueId.fromByteArray(buffer.readByteArray());

		this.typeRaw = buffer.readString();

		byte[] contentAsByteArray = buffer.readByteArray();
		ItemStack[] content = NmsInstance.binaryToItemStackArray(contentAsByteArray).toArray(ItemStack[]::new);
		this.content = content;
	}
}
