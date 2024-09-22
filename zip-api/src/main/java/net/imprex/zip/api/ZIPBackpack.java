package net.imprex.zip.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ZIPBackpack {

	void save();

	void open(Player player);
	
	void updateItem(ItemStack item);

	boolean applyOnItem(ItemStack item);

	boolean hasContent();

	boolean hasUnuseableContent();

	boolean giveUnsueableContent(Player player);

	boolean isValid();

	Inventory getInventory();

	ZIPBackpackType getType();

	ZIPUniqueId getId();
}
