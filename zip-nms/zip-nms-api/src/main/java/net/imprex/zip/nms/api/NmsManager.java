package net.imprex.zip.nms.api;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public interface NmsManager {

	byte[] itemstackToBinary(ItemStack[] items);

	List<ItemStack> binaryToItemStack(byte[] binary);

	void setSkullProfile(SkullMeta meta, String texture);

	boolean isAir(Material material);
}