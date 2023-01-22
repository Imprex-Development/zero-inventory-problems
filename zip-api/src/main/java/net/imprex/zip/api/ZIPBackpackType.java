package net.imprex.zip.api;

import org.bukkit.inventory.ItemStack;

public interface ZIPBackpackType {

	ItemStack createItem();

	ZIPBackpack create();

	int getInventoryRows();

	String getUniqueName();

	String getDisplayName();

	ItemStack getItem();

	ZIPRecipe getRecipe();
}