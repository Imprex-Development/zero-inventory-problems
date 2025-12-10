package dev.imprex.zip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.gson.JsonObject;

import dev.imprex.zip.api.ZIPBackpack;
import dev.imprex.zip.api.ZIPBackpackType;
import dev.imprex.zip.api.ZIPHandler;
import dev.imprex.zip.api.ZIPUniqueId;
import dev.imprex.zip.common.UniqueId;
import dev.imprex.zip.common.ZIPLogger;

public class BackpackHandler implements ZIPHandler {

	private final BackpackPlugin plugin;
	private final BackpackRegistry registry;

	private final NamespacedKey backpackStorageKey;
	private final NamespacedKey backpackIdentifierKey;
	private final Path folderPath;

	private Map<UniqueId, Backpack> backpackById = new HashMap<>();
	private Map<Inventory, Backpack> backpackByInventory = new HashMap<>();
	private List<UniqueId> loadingIssue = new ArrayList<>();

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
		this.backpackById.clear();
		this.backpackByInventory.clear();
	}

	private Backpack loadBackpack(UniqueId id) {
		Path file = this.getPathForId(id);
		
		if (!Files.isRegularFile(file)) {
			// migrate backpack to json
			if (!BackpackMigrator.migrate(this.folderPath, id)) {
				return null;
			}
			
			// backpack was successful migrated
		}

		try {
			JsonObject json;
			try (FileInputStream inputStream = new FileInputStream(file.toFile());
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
				json = Backpack.GSON.fromJson(inputStreamReader, JsonObject.class);
			}
			
			Backpack backpack = new Backpack(this.plugin, id, json);
			return backpack;
		} catch (Exception e) {
			ZIPLogger.error("Unable to load backpack for id '" + file.getFileName().toString() + "'", e);
		}
		return null;
	}

	@Override
	public void save(ZIPBackpack backpack) {
		if (Files.notExists(this.folderPath)) {
			try {
				Files.createDirectories(this.folderPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		JsonObject json = new JsonObject();
		if (backpack instanceof Backpack internalBackpack) {
			internalBackpack.save(json);
		} else {
			throw new IllegalArgumentException("Backpack object is not a ZIPBackpack");
		}

		Path file = this.getPathForId(backpack.getId());
		try (FileOutputStream outputStream = new FileOutputStream(file.toFile());
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			Backpack.GSON.toJson(json, outputStreamWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Backpack getBackpack(UniqueId id) {
		if (this.loadingIssue.contains(id)) {
			return null;
		}
		
		Backpack backpack = this.backpackById.get(id);
		if (backpack != null) {
			return backpack;
		}

		backpack = this.loadBackpack(id);
		if (backpack == null) {
			this.loadingIssue.add(id);
		}
		return backpack;
	}

	@Override
	public Backpack getBackpack(ZIPUniqueId id) {
		Backpack backpack = this.backpackById.get((UniqueId) id);
		if (backpack == null) {
			backpack = this.getBackpack((UniqueId) id);
		}
		return backpack;
	}

	@Override
	public Backpack getBackpack(Inventory inventory) {
		return this.backpackByInventory.get(inventory);
	}

	@Override
	public Backpack getBackpack(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

			UniqueId uniqueId = null;
			if (dataContainer.has(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY)) {
				byte[] storageKey = dataContainer.get(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY);
				uniqueId = UniqueId.fromByteArray(storageKey);

				Backpack backpack = this.getBackpack(uniqueId);
				if (backpack != null) {
					return backpack;
				}
			}
			
			if (this.loadingIssue.contains(uniqueId)) {
				return null;
			}
			
			if (dataContainer.has(this.backpackIdentifierKey, PersistentDataType.STRING)) {
				String backpackIdentifier = dataContainer.get(this.backpackIdentifierKey, PersistentDataType.STRING);
				BackpackType backpackType = this.registry.getTypeByName(backpackIdentifier);
				if (backpackType == null) {
					return null;
				}

				Backpack backpack = uniqueId != null
					? new Backpack(plugin, backpackType, uniqueId)
					: backpackType.create();
				backpack.applyOnItem(item);
				return backpack;
			}
		}

		return null;
	}

	@Override
	public ZIPBackpackType getBackpackType(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

			if (dataContainer.has(this.backpackIdentifierKey, PersistentDataType.STRING)) {
				String backpackIdentifier = dataContainer.get(this.backpackIdentifierKey, PersistentDataType.STRING);
				BackpackType backpackType = this.registry.getTypeByName(backpackIdentifier);
				return backpackType;
			}
		}

		return null;
	}

	@Override
	public boolean isBackpack(ItemStack item) {
		if (item == null) {
			return false;
		}

		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			return meta.getPersistentDataContainer().has(this.backpackIdentifierKey, PersistentDataType.STRING);
		}

		return false;
	}
	
	@Override
	public UniqueId getUniqueId(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

			if (dataContainer.has(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY)) {
				byte[] storageKey = dataContainer.get(this.backpackStorageKey, PersistentDataType.BYTE_ARRAY);
				return UniqueId.fromByteArray(storageKey);
			}
		}

		return null;
	}
	
	private Path getPathForId(ZIPUniqueId id) {
		return this.folderPath.resolve(id.toString() + ".json");
	}
	
	public Path getFolderPath() {
		return this.folderPath;
	}
}