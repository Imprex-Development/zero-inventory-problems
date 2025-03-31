package net.imprex.zip.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.api.ZIPBackpackHistory.HistoryConsumer;

public interface ZIPBackpack {

	void save();

	void open(Player player);
	
	void updateItem(ItemStack item);

	boolean applyOnItem(ItemStack item);

	boolean hasContent();

	boolean hasUnuseableContent();

	boolean giveUnsueableContent(Player player);

	List<ZIPBackpackHistory> getHistroy();

	void computeHistory(UUID uuid, OffsetDateTime dateTime, HistoryConsumer consumer);

	default void computeHistory(UUID uuid, HistoryConsumer consumer) {
		this.computeHistory(uuid,  OffsetDateTime.now(), consumer);
	}

	void addHistory(UUID uuid, OffsetDateTime dateTime, Map<ItemStack, Integer> items);

	default void addHistory(UUID uuid, Map<ItemStack, Integer> items) {
		this.addHistory(uuid, OffsetDateTime.now(), items);
	}

	void clearHistory();

	boolean isValid();

	Inventory getInventory();

	ZIPBackpackType getType();

	ZIPUniqueId getId();
}
