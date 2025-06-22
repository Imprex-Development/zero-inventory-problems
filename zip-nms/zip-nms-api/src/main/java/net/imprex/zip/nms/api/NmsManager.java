package net.imprex.zip.nms.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;

public interface NmsManager {

	JsonObject itemstackToJsonElement(ItemStack[] items);

	ItemStack[] jsonElementToItemStack(JsonObject jsonElement);
	
	JsonObject migrateToJsonElement(byte[] binary);

	void setSkullProfile(SkullMeta meta, String texture);

	boolean isAir(Material material);
}