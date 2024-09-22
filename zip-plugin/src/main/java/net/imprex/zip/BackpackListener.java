package net.imprex.zip;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.imprex.zip.api.ZIPBackpackType;
import net.imprex.zip.config.MessageConfig;
import net.imprex.zip.config.MessageKey;

public class BackpackListener implements Listener {

	private final BackpackHandler backpackHandler;
	private final BackpackRegistry backpackRegistry;
	private final UpdateSystem updateSystem;
	private final MessageConfig messageConfig;

	public BackpackListener(BackpackPlugin plugin) {
		this.backpackHandler = plugin.getBackpackHandler();
		this.backpackRegistry = plugin.getBackpackRegistry();
		this.updateSystem = plugin.getUpdateSystem();
		this.messageConfig = plugin.getBackpackConfig().message();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		this.backpackRegistry.discoverRecipes(player);

		if (player.isOp() || player.hasPermission("zeroinventoryproblems.notification")) {
			this.updateSystem.checkForUpdates(player);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack currentItem = event.getCurrentItem();
		if (event.getClick() == ClickType.NUMBER_KEY) {
			int hotbarSlot = event.getHotbarButton();
			if (hotbarSlot != -1) {
				currentItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);
			}
		}
		
		if (!this.backpackHandler.isBackpack(currentItem)) {
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
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		Backpack backpack = this.backpackHandler.getBackpack(item);
		if (backpack != null) {
			// update backpack attributes on pickup
			// for example when the texture has changed
			backpack.updateItem(item);
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();
		Backpack backpack = this.backpackHandler.getBackpack(item);
		if (backpack != null) {
			event.getPlayer().closeInventory();
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getHand() == EquipmentSlot.HAND
				&& this.backpackHandler.isBackpack(event.getItem())) {
			event.setCancelled(true);

			Backpack backpack = this.backpackHandler.getBackpack(event.getItem());
			if (backpack != null) {
				backpack.open(event.getPlayer());
			} else {
				this.messageConfig.send(event.getPlayer(), MessageKey.UnableToLoadBackpack);
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
		HumanEntity player = event.getWhoClicked();
		ItemStack item = event.getCurrentItem();

		ZIPBackpackType type = this.backpackHandler.getBackpackType(item);
		if (type != null && type.hasCraftingPermission()) {
			String permission = type.getCraftingPermission();
			if (!player.hasPermission(permission)) {
				event.setCancelled(true);

				this.messageConfig.send(player, MessageKey.YouDontHaveTheFollowingPermission, permission);
			}
		}
	}
}