package dev.imprex.zip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.imprex.zip.config.MessageConfig;
import dev.imprex.zip.config.MessageKey;
import dev.imprex.zip.nms.api.ItemStackContainerResult;
import dev.imprex.zip.nms.api.ItemStackWithSlot;
import dev.imprex.zip.api.ZIPBackpack;
import dev.imprex.zip.common.BPConstants;
import dev.imprex.zip.common.UniqueId;
import dev.imprex.zip.common.ZIPLogger;

public class Backpack implements ZIPBackpack {
	
	static final Gson GSON = new Gson();

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

	public Backpack(BackpackPlugin plugin, UniqueId id, JsonObject json) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.messageConfig = plugin.getBackpackConfig().message();
		this.identifierKey = plugin.getBackpackIdentifierKey();
		this.storageKey = plugin.getBackpackStorageKey();

		/*
		 * Load backpack id from buffer but don't use it!
		 * Just for later migration to SQL
		 */
		
		
		this.id = id;

		this.typeRaw = json.get(BPConstants.KEY_TYPE_RAW).getAsString();
		this.type = plugin.getBackpackRegistry().getTypeByName(this.typeRaw);

		JsonObject contentAsJson = json.getAsJsonObject(BPConstants.KEY_INVENTORY);
		ItemStackContainerResult contentResult = NmsInstance.jsonElementToItemStack(contentAsJson);
		ItemStack[] content = this.parseItemStackList(contentResult);
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
	
	private ItemStack[] parseItemStackList(ItemStackContainerResult result) {
		int containerSize = result.containerSize();
		
		ItemStack[] items = new ItemStack[containerSize];
		Arrays.fill(items, new ItemStack(Material.AIR));
		
		List<ItemStack> duplicateSlot = null;
		
		for (ItemStackWithSlot itemWithSlot : result.items()) {
			ItemStack item = itemWithSlot.item();
			int slot = itemWithSlot.slot();
			
			if (containerSize <= slot) {
				// something went wrong !? maybe user modified it him self
				ZIPLogger.warn("Slot size was extended from " + containerSize + " to " + slot + " this should not happen. Do not change the slot number inside the config manually!?");
				
				ItemStack[] newItems = new ItemStack[slot + 1];
				System.arraycopy(items, 0, newItems, 0, items.length);
				Arrays.fill(newItems, items.length, newItems.length, new ItemStack(Material.AIR));
				items = newItems;
			}
			
			if (items[slot].getType() != Material.AIR) {
				if (duplicateSlot == null) {
					duplicateSlot = new ArrayList<>();
				}
				duplicateSlot.add(item);
				ZIPLogger.warn("Duplicate item found on slot " + slot + " this should not happen. Do not change the slot number inside the config manually!?");
			} else {
				items[slot] = item;
			}
		}
		
		// fill existing empty slots with duplicate item
		while (duplicateSlot != null && !duplicateSlot.isEmpty()) {
			outher: for (Iterator<ItemStack> iterator = duplicateSlot.iterator(); iterator.hasNext();) {
				ItemStack itemStack = (ItemStack) iterator.next();
				
				for (int i = 0; i < items.length; i++) {
					if (items[i].getType() == Material.AIR) {
						items[i] = itemStack;
						iterator.remove();
						break;
					} else if (i == items.length - 1) {
						break outher;
					}
				}
			}

			// extend slot limit and try again
			if (!duplicateSlot.isEmpty()) {
				int extendedSlots = items.length + duplicateSlot.size();
				ItemStack[] newItems = new ItemStack[extendedSlots];
				System.arraycopy(items, 0, newItems, 0, items.length);
				Arrays.fill(newItems, items.length, newItems.length, new ItemStack(Material.AIR));
				items = newItems;
			}
		}
		
		return items;
	}

	public void save(JsonObject json) {
		if (this.inventory != null) {
			for (int i = 0; i < this.inventory.getSize(); i++) {
				this.content[i] = this.inventory.getItem(i);
			}
		} else if (this.content == null) {
			throw new NullPointerException("content can not be null");
		}

		json.addProperty(BPConstants.KEY_VERSION, BPConstants.VERSION);
		json.addProperty(BPConstants.KEY_ID, this.id.toString());
		json.addProperty(BPConstants.KEY_TYPE_RAW, this.typeRaw);
		json.add(BPConstants.KEY_INVENTORY, NmsInstance.itemstackToJsonElement(this.content));
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
		
		this.save();
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