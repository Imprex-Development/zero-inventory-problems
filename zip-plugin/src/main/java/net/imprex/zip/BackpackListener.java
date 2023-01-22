package net.imprex.zip;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BackpackListener implements Listener {

	private final BackpackHandler backpackHandler;
	private final BackpackRegistry backpackRegistry;
	private final UpdateSystem updateSystem;

	public BackpackListener(BackpackPlugin plugin) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.backpackRegistry = plugin.getBackpackRegistry();
		this.updateSystem = plugin.getUpdateSystem();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		for (BackpackType backpackType : this.backpackRegistry.getTypes()) {
			BackpackRecipe recipe = backpackType.getRecipe();
			if (recipe.canDiscover()) {
				player.discoverRecipe(recipe.getKey());
			}
		}

		if (player.isOp() || player.hasPermission("zeroinventoryproblems.notification")) {
			this.updateSystem.checkForUpdates(player);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		if (!this.backpackHandler.isBackpack(event.getCurrentItem())) {
			return;
		}

		Inventory topInventory = event.getWhoClicked().getOpenInventory().getTopInventory();
		if (topInventory == null) {
			return;
		}

		Backpack backpack = this.backpackHandler.getBackpack(topInventory);
		if (backpack != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory topInventory = event.getPlayer().getOpenInventory().getTopInventory();
		if (topInventory != null) {
			Backpack backpack = this.backpackHandler.getBackpack(topInventory);
			if (backpack != null) {
				backpack.save();
			}
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Backpack backpack = this.backpackHandler.getBackpack(event.getItemDrop().getItemStack());
		if (backpack != null) {
			event.getPlayer().closeInventory();
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getHand() == EquipmentSlot.HAND) {
			if (this.backpackHandler.isBackpack(event.getItem())) {
				event.setCancelled(true);
			}

			Backpack backpack = this.backpackHandler.getBackpack(event.getItem());
			if (backpack != null) {
				backpack.open(event.getPlayer());
			}
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (this.backpackHandler.isBackpack(event.getItemInHand())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onCraftItem(CraftItemEvent event) {
		ItemStack item = event.getCurrentItem();
		if (this.backpackHandler.isBackpack(item)) {
			if (event.isShiftClick()) {
				event.setCancelled(true);
				return;
			}

			this.backpackHandler.getBackpack(item);
		}
	} 
}