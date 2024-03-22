package net.imprex.zip.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ZIPHandler {

	void save(ZIPBackpack backpack);

	ZIPBackpack getBackpack(ZIPUniqueId id);

	ZIPBackpack getBackpack(Inventory inventory);

	ZIPBackpack getBackpack(ItemStack item);

	ZIPBackpackType getBackpackType(ItemStack item);

	boolean isBackpack(ItemStack item);
}
