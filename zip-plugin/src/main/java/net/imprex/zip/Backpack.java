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

import net.imprex.zip.api.ZIPBackpack;
import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;

public class Backpack implements ZIPBackpack {

	private final BackpackHandler backpackHandler;
	private final MessageConfig messageConfig;

	private final NamespacedKey identifierKey;
	private final NamespacedKey storageKey;

	private final UniqueId id;

	private final String typeRaw;
	private final BackpackType type;

	private ItemStack[] content;
	private Inventory inventory;

	public Backpack(BackpackPlugin plugin, BackpackType type, UniqueId id) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.identifierKey = plugin.getBackpackIdentifierKey();
		this.storageKey = plugin.getBackpackStorageKey();

		this.type = type;
		this.typeRaw = type.getUniqueName();
		this.id = id != null ? id : UniqueId.get();

		int rows = this.type.getInventoryRows();
		String displayName = this.type.getDisplayName();
		this.inventory = Bukkit.createInventory(null, 9 * rows, displayName);
		this.content = this.inventory.getContents();

		this.backpackHandler.registerBackpack(this);
		
		this.save();
	}

	public Backpack(BackpackPlugin plugin, UniqueId id, Ingrim4Buffer buffer) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.identifierKey = plugin.getBackpackIdentifierKey();
		this.storageKey = plugin.getBackpackStorageKey();

		/*
		 * Load backpack id from buffer but don't use it!
		 * Just for later migration to SQL
		 */
		buffer.readByteArray();
		this.id = id;

		this.typeRaw = buffer.readString();
		this.type = plugin.getBackpackRegistry().getTypeByName(this.typeRaw);

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
		buffer.writeString(this.typeRaw);
		buffer.writeByteArray(NmsInstance.itemstackToBinary(this.content));
	}

	@Override
	public void save() {
		this.backpackHandler.save(this);
	}

	@Override
	public void open(Player player) {
		Objects.nonNull(player);

		if (this.inventory != null) {
			player.openInventory(this.inventory);

			if (this.hasUnuseableContent()) {
				this.messageConfig.send(player, MessageKey.YouHaveUnusableItemsUsePickup);
			}
		} else {
			player.sendMessage(this.messageConfig.get(MessageKey.ThisBackpackNoLongerExist));
			
			if (this.hasUnuseableContent()) {
				this.messageConfig.send(player, MessageKey.YouHaveUnusableItemsUsePickup);
			}
		}
	}

	@Override
	public boolean applyOnItem(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			meta.getPersistentDataContainer().set(this.storageKey, PersistentDataType.BYTE_ARRAY, this.id.toByteArray());
			meta.getPersistentDataContainer().set(this.identifierKey, PersistentDataType.STRING, this.typeRaw);
			item.setItemMeta(meta);
			return true;
		}
		return false;
	}

	@Override
	public boolean hasContent() {
		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getSize(); i++) {
				ItemStack item = this.inventory.getItem(i);
				if (item != null && !NmsInstance.isAir(item.getType())) {
					return true;
				}
			}
		}
		return this.hasUnuseableContent();
	}

	@Override
	public boolean hasUnuseableContent() {
		int contentSize = this.content.length;
		int inventorySize = this.inventory != null ? this.inventory.getSize() : 0;
		if (inventorySize >= contentSize) {
			return false;
		}

		for (int i = inventorySize; i < contentSize; i++) {
			ItemStack item = this.content[i];
			if (item != null && item.getType() != Material.AIR) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean giveUnsueableContent(Player player) {
		PlayerInventory inventory = player.getInventory();
		boolean empty = true;

		int inventorySize = this.inventory != null ? this.inventory.getSize() : 0;
		for (int i = inventorySize; i < this.content.length; i++) {
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

	@Override
	public boolean isValid() {
		return this.inventory != null && this.type != null && this.content != null;
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public BackpackType getType() {
		return this.type;
	}

	@Override
	public UniqueId getId() {
		return this.id;
	}
}