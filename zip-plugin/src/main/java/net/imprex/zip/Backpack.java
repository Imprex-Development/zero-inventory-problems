package net.imprex.zip;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.collect.Lists;

import net.imprex.zip.api.ZIPBackpack;
import net.imprex.zip.api.ZIPBackpackHistory;
import net.imprex.zip.api.ZIPBackpackHistory.HistoryConsumer;
import net.imprex.zip.common.Ingrim4Buffer;
import net.imprex.zip.common.UniqueId;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;
import net.imprex.zip.migration.BackpackMigrator;
import net.imprex.zip.util.FixedSizeQueue;

public class Backpack implements ZIPBackpack {

	public static final int VERSION = 100;

	private final BackpackHandler backpackHandler;
	private final MessageConfig messageConfig;

	private final NamespacedKey identifierKey;
	private final NamespacedKey storageKey;

	private final UniqueId id;

	private final String typeRaw;
	private final BackpackType type;

	private final FixedSizeQueue<BackpackHistory> history;

	private ItemStack[] content;
	private Inventory inventory;

	private final Map<Player, BackpackTransferPlayer> transfer = new WeakHashMap<>();

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

		this.history = new FixedSizeQueue<>(plugin.getBackpackConfig().general().historySize);

		this.backpackHandler.registerBackpack(this);
		
		this.save();
	}

	public Backpack(BackpackPlugin plugin, UniqueId id, Ingrim4Buffer buffer) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.identifierKey = plugin.getBackpackIdentifierKey();
		this.storageKey = plugin.getBackpackStorageKey();

		try {
			// pooled buffer
			buffer = BackpackMigrator.migrate(plugin, buffer);

			// read id
			this.id = UniqueId.fromByteArray(buffer.readByteArray());

			// read type
			this.typeRaw = buffer.readString();
			this.type = plugin.getBackpackRegistry().getTypeByName(this.typeRaw);

			// read content
			byte[] contentAsByteArray = buffer.readByteArray();
			ItemStack[] content = NmsInstance.binaryToItemStackArray(contentAsByteArray).toArray(ItemStack[]::new);
			this.content = content;

			// read history
			this.history = new FixedSizeQueue<>(plugin.getBackpackConfig().general().historySize);
			int historySize = buffer.readInt();
			for (int i = 0; i < historySize; i++) {
				this.history.add(BackpackHistory.read(buffer));
			}
		} finally {
			// release buffer
			buffer.release();
			buffer = null;
		}

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

		// write latest version
		buffer.writeInt(VERSION);

		// write id, type and content
		buffer.writeByteArray(this.id.toByteArray());
		buffer.writeString(this.typeRaw);
		buffer.writeByteArray(NmsInstance.itemstackArrayToBinary(this.content));

		// write history
		buffer.writeInt(this.history.size());
		this.history.forEach(history -> history.write(buffer));
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
	public void updateItem(ItemStack item) {
		this.type.updateItem(item);
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

		BackpackHistory.create(player.getUniqueId(), content, content);

		return empty;
	}

	@Override
	public List<ZIPBackpackHistory> getHistroy() {
		return Collections.unmodifiableList(Lists.newArrayList(this.history.collection()));
	}

	@Override
	public void computeHistory(UUID uuid, OffsetDateTime dateTime, HistoryConsumer consumer) {
		ItemStack[] previous = this.content.clone();
		consumer.consume();

		Map<ItemStack, Integer> difference = BackpackHistory.difference(previous, this.content);
		if (!difference.isEmpty()) {
			this.history.add(new BackpackHistory(uuid, dateTime, difference));
		}
	}

	@Override
	public void addHistory(UUID uuid, OffsetDateTime dateTime, Map<ItemStack, Integer> items) {
		this.history.add(new BackpackHistory(uuid, dateTime, items));
	}

	@Override
	public void clearHistory() {
		this.history.clear();
	}

	public void addToTransfer(Player player) {
		BackpackTransferPlayer previous = this.transfer.put(player, new BackpackTransferPlayer());
		if (!(previous == null || previous.isEmpty())) {
			// close inventory was not triggered
			this.addHistory(player.getUniqueId(), previous.getCreated(), previous.getItems());
		}
	}

	public BackpackTransferPlayer getTransfer(Player player) {
		return this.transfer.computeIfAbsent(player, target -> new BackpackTransferPlayer());
	}

	public void closeTransfer(Player player) {
		BackpackTransferPlayer transfer = this.transfer.remove(player);
		if (transfer == null || transfer.isEmpty()) {
			return;
		}

		this.addHistory(player.getUniqueId(), transfer.getItems());
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