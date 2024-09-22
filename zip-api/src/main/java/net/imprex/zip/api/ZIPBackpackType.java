package net.imprex.zip.api;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public interface ZIPBackpackType {

	ItemStack createItem();

	ZIPBackpack create();

	void updateItem(ItemStack item);

	int getInventoryRows();

	String getUniqueName();

	String getDisplayName();
	
	String getItemTexture();
	
	List<String> getLore();
	
	int getCustomModelData();

	ItemStack getItem();

	ZIPRecipe getRecipe();

	boolean hasCraftingPermission();

	String getCraftingPermission();
}