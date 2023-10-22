package net.imprex.zip.api;

import java.util.concurrent.CompletableFuture;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface ZIPHandler {

	CompletableFuture<Void> save(ZIPBackpack backpack);

	CompletableFuture<ZIPBackpack> getBackpack(ZIPUniqueId id);

	CompletableFuture<ZIPBackpack> getBackpack(ItemStack item);

	ZIPBackpack getLoadedBackpack(Inventory inventory);

	ZIPUniqueId getBackpackId(ItemStack item);

	boolean isBackpack(ItemStack item);
}
