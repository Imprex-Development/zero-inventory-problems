package net.imprex.zip;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import net.imprex.zip.api.ZIPBackpackHistory;
import net.imprex.zip.common.Ingrim4Buffer;

public record BackpackHistory(UUID player, OffsetDateTime dateTime, Map<ItemStack, Integer> items) implements ZIPBackpackHistory {

	public static BackpackHistory read(Ingrim4Buffer buffer) {
		UUID player = buffer.readUUID();
		OffsetDateTime dateTime = buffer.readDateTime();

		Map<ItemStack, Integer> content = new HashMap<>();
		for (int i = 0; i < buffer.readInt(); i++) {
			content.put(
					NmsInstance.binaryToItemStack(buffer.readByteArray()),
					buffer.readInt());
		}

		return new BackpackHistory(player, dateTime, content);
	}

	public void write(Ingrim4Buffer buffer) {
		buffer.writeUUID(this.player);
		buffer.writeDateTime(this.dateTime);

		buffer.writeInt(this.items.size());
		for (Entry<ItemStack, Integer> entry : this.items.entrySet()) {
			buffer.writeByteArray(NmsInstance.itemstackToBinary(entry.getKey()));
			buffer.writeInt(entry.getValue());
		}
	}

	public static ZIPBackpackHistory create(UUID player, ItemStack[] previous, ItemStack[] newest) {
		return new BackpackHistory(player, OffsetDateTime.now(), difference(previous, newest));
	}

	public static Map<ItemStack, Integer> difference(ItemStack[] originalStack, ItemStack[] compareStack) {
		Map<ItemStack, Integer> originalList = merge(originalStack);
		Map<ItemStack, Integer> compareList = merge(compareStack);
		Map<ItemStack, Integer> diffList = new HashMap<>();

		// check if amount changed or items were removed
		check: for (Entry<ItemStack, Integer> entry : originalList.entrySet()) {
			ItemStack itemStack = entry.getKey();
			int originalAmount = entry.getValue();

			for (Iterator<Entry<ItemStack, Integer>> iterator = compareList.entrySet().iterator(); iterator.hasNext();) {
				Entry<ItemStack, Integer> compareEntry = iterator.next();
				// ignore not equals items
				if (!itemStack.isSimilar(compareEntry.getKey())) {
					continue;
				}

				// we already handled this item, so we don't need to check it again
				iterator.remove();

				int compareAmount = compareEntry.getValue();
				// item amount is the same
				if (originalAmount == compareAmount) {
					continue check;
				}

				// calculate item amount difference
				int diffAmount = compareAmount - originalAmount;
				diffList.put(itemStack, diffAmount);
				continue check;
			}

			// item was removed
			diffList.put(itemStack, -originalAmount);
		}

		// new added items
		// we already check the existing items and compared them so only non previous existing items are remaining
		diffList.putAll(compareList);
		return diffList;
	}

	public static Map<ItemStack, Integer> merge(ItemStack[] itemStack) {
		Map<ItemStack, Integer> newStack = new HashMap<>();
		check: for (ItemStack checkStack : itemStack) {
			for (Entry<ItemStack, Integer> diffStack : newStack.entrySet()) {
				if (diffStack.getKey().isSimilar(checkStack)) {
					diffStack.setValue(diffStack.getValue() + checkStack.getAmount());
					continue check;
				}
			}

			newStack.put(checkStack.clone(), checkStack.getAmount());
		}
		return newStack;
	}
}