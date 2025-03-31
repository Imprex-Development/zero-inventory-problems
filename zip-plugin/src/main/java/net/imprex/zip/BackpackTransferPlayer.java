package net.imprex.zip;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BackpackTransferPlayer {

	private final OffsetDateTime created = OffsetDateTime.now();

	private final Map<ItemStack, Integer> items = new HashMap<>();

	public void track(ItemStack item, int amount) {
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		for (Entry<ItemStack, Integer> entry : this.items.entrySet()) {
			if (entry.getKey().isSimilar(item)) {
				entry.setValue(entry.getValue() + amount);
				return;
			}
		}

		this.items.put(item, amount);
	}

	public boolean isEmpty() {
		return this.items.isEmpty();
	}

	public Map<ItemStack, Integer> getItems() {
		return this.items;
	}

	public OffsetDateTime getCreated() {
		return this.created;
	}
}
