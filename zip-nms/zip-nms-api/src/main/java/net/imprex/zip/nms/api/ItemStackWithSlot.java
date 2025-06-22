package net.imprex.zip.nms.api;

import org.bukkit.inventory.ItemStack;

public record ItemStackWithSlot(int slot, ItemStack item) {
}
