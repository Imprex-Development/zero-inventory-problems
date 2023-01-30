package net.imprex.zip.storage;

import org.bukkit.inventory.ItemStack;

import net.imprex.zip.api.ZIPUniqueId;

public record StorageData(ZIPUniqueId id, String type, ItemStack[] content) {
}
