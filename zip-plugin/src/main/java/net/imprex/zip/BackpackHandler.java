package net.imprex.zip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.io.ByteStreams;

import io.netty.buffer.Unpooled;
import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;

public class BackpackHandler {

	private final BackpackPlugin plugin;
	private final BackpackRegistry registry;

	private final NamespacedKey backpackStorageKey;
	private final NamespacedKey backpackIdentifierKey;
	private final Path folderPath;

	private Map<UniqueId, Backpack> backpackById = new ConcurrentHashMap<>();
	private Map<ItemStack, Backpack> backpackByItem = new ConcurrentHashMap<>();
	private Map<Inventory, Backpack> backpackByInventory = new ConcurrentHashMap<>();

	public BackpackHandler(BackpackPlugin plugin) {
		this.plugin = plugin;
		this.registry = plugin.getBackpackRegistry();
		this.backpackStorageKey = plugin.getBackpackStorageKey();
		this.backpackIdentifierKey = plugin.getBackpackIdentifierKey();

		this.folderPath = Path.of(plugin.getDataFolder().getAbsolutePath(), "storage");
	}

	void registerBackpack(Backpack backpack) {
		this.backpackById.put(backpack.getId(), backpack);

		if (backpack.isValid()) {
			this.backpackByInventory.put(backpack.getInventory(), backpack);
		}
	}

	public void disable() {
		this.backpackById.values().forEach(Backpack::save);
	}

	public void loadBackpacks() {
		try {
			if (Files.notExists(this.folderPath)) {
				Files.createDirectories(this.folderPath);
			}

			Files.walk(this.folderPath, FileVisitOption.FOLLOW_LINKS).forEach(this::loadBackpack);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Backpack loadBackpack(Path file) {
		if (!Files.isRegularFile(file)) {
			return null;
		}

		try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
			byte[] data = ByteStreams.toByteArray(inputStream);
			Ingrim4Buffer buffer = new Ingrim4Buffer(Unpooled.wrappedBuffer(data));

			Backpack backpack = new Backpack(this.plugin, buffer);
			this.backpackById.put(backpack.getId(), backpack);

			return backpack;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void save(Backpack backpack) {
		if (Files.notExists(this.folderPath)) {
			try {
				Files.createDirectories(this.folderPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Path file = this.folderPath.resolve(backpack.getId().toString());
		try (FileOutputStream outputStream = new FileOutputStream(file.toFile())) {
			Ingrim4Buffer buffer = new Ingrim4Buffer(Unpooled.buffer());
			backpack.save(buffer);

			byte[] bytes = new byte[buffer.readableBytes()];
			buffer.readBytes(bytes);
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Backpack getBackpack(UniqueId id) {
		return this.backpackById.get(id);
	}

	public Backpack getBackpack(Inventory inventory) {
		return this.backpackByInventory.get(inventory);
	}

	public Backpack getBackpack(ItemStack item) {
		if (item == null) {
			return null;
		}

		Backpack backpack = this.backpackByItem.get(item);
		if (backpack != null) {
			return backpack;
		}

		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

			if (dataContainer.has(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY)) {
				byte[] storageKey = dataContainer.get(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY);
				UniqueId uniqueId = UniqueId.fromByteArray(storageKey);
				return this.getBackpack(uniqueId);
			} else if (dataContainer.has(this.backpackIdentifierKey, PersistentDataType.STRING)) {
				String backpackIdentifier = dataContainer.get(this.backpackIdentifierKey, PersistentDataType.STRING);
				BackpackType backpackType = this.registry.getTypeByName(backpackIdentifier);
				if (backpackType == null) {
					return null;
				}

				Backpack newBackpack = backpackType.create();
				newBackpack.applyOnItem(item);
				return newBackpack;
			}
		}

		return null;
	}

	public boolean isBackpack(ItemStack item) {
		if (item == null) {
			return false;
		}

		Backpack backpack = this.backpackByItem.get(item);
		if (backpack != null) {
			return true;
		}

		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			return meta.getPersistentDataContainer().has(this.backpackIdentifierKey, PersistentDataType.STRING);
		}

		return false;
	}
}