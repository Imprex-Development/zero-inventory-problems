package net.imprex.zip.api;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public interface ZIPBackpackHistory {

	UUID player();

	OffsetDateTime dateTime();

	Map<ItemStack, Integer> items();

	public static interface HistoryConsumer {
		
		void consume();
	}
}