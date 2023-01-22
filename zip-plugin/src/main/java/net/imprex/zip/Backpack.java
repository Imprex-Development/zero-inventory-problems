package net.imprex.zip;

import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;

public class Backpack {

	private final BackpackHandler backpackHandler;
	private final NamespacedKey storageKey;
	private final MessageConfig messageConfig;

	private final UniqueId id;
	private final BackpackType type;

	private ItemStack[] content;
	private Inventory inventory;

	public Backpack(BackpackPlugin plugin, BackpackType type) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.storageKey = plugin.getBackpackStorageKey();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.type = type;

		this.id = UniqueId.get();

		int rows = this.type.getInventoryRows();
		String displayName = this.type.getDisplayName();
		this.inventory = Bukkit.createInventory(null, 9 * rows, displayName);
		this.content = this.inventory.getContents();

		this.backpackHandler.registerBackpack(this);
	}

	public Backpack(BackpackPlugin plugin, Ingrim4Buffer buffer) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.storageKey = plugin.getBackpackStorageKey();
		this.messageConfig = plugin.getBackpackConfig().message();

		this.id = UniqueId.fromByteArray(buffer.readByteArray());

		String typeName = buffer.readString();
		this.type = plugin.getBackpackRegistry().getTypeByName(typeName);

		byte[] contentAsByteArray = buffer.readByteArray();
		ItemStack[] content = NmsInstance.binaryToItemStack(contentAsByteArray).toArray(ItemStack[]::new);
		this.content = content;

		if (this.type != null) {
			String displayName = this.type.getDisplayName();
			int rows = this.type.getInventoryRows();
			int size = rows * 9;

			this.inventory = Bukkit.createInventory(null, size, displayName);

			if (size < content.length) {
				for (int i = 0; i < this.inventory.getContents().length; i++) {
					this.inventory.setItem(i, content[i]);
				}
			} else {
				this.inventory.setContents(content);
			}

			if (this.content.length < this.inventory.getSize()) {
				this.content = this.inventory.getContents();
			}
		}

		this.backpackHandler.registerBackpack(this);
	}

	public void save(Ingrim4Buffer buffer) {
		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getSize(); i++) {
				this.content[i] = this.inventory.getItem(i);
			}
		} else if (this.content == null) {
			throw new NullPointerException("content can not be null");
		}

		buffer.writeByteArray(this.id.toByteArray());
		buffer.writeString(this.type.getUniqueName());
		buffer.writeByteArray(NmsInstance.itemstackToBinary(this.content));
	}

	public void save() {
		this.backpackHandler.save(this);
	}

	public void open(Player player) {
		Objects.nonNull(player);

		if (this.inventory != null) {
			player.openInventory(this.inventory);

			if (this.hasUnuseableItem()) {
				this.messageConfig.send(player, MessageKey.YouHaveUnuseableItemsUsePickup);
			}
		} else {
			player.sendMessage(this.messageConfig.get(MessageKey.AErrorOccured));
		}
	}

	public boolean applyOnItem(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			meta.getPersistentDataContainer().set(this.storageKey, PersistentDataType.BYTE_ARRAY, id.toByteArray());
			item.setItemMeta(meta);
			return true;
		}
		return false;
	}

	public boolean hasContent() {
		for (int i = 0; i < this.inventory.getSize(); i++) {
			ItemStack item = this.inventory.getItem(i);
			if (item != null && !NmsInstance.isAir(item.getType())) {
				return true;
			}
		}
		return false;
	}

	public boolean hasUnuseableItem() {
		int shadowContentSize = this.content.length;
		if (this.inventory.getSize() >= shadowContentSize) {
			return false;
		}

		for (int i = this.inventory.getSize(); i < shadowContentSize; i++) {
			ItemStack item = this.content[i];
			if (item != null && item.getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}

	public boolean giveUnsueableItems(Player player) {
		PlayerInventory inventory = player.getInventory();
		boolean empty = true;

		for (int i = this.inventory.getSize(); i < this.content.length; i++) {
			ItemStack item = this.content[i];
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}

			Map<Integer, ItemStack> unused = inventory.addItem(item);
			if (unused.size() != 0) {
				this.content[i] = unused.get(0);
				empty = false;
			} else {
				this.content[i] = null;
			}
		}
		return empty;
	}

	public boolean isValid() {
		return this.inventory != null && this.type != null && this.content != null;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public BackpackType getType() {
		return this.type;
	}

	public UniqueId getId() {
		return this.id;
	}
}