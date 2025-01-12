package net.imprex.zip;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
	public void onInventoryClickBackpack(InventoryClickEvent event) {
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

	/**
	 * Handle priority events first and only log to history if not cancelled
	 * 
	 * @param event
	 */
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onInventoryClickHistory(InventoryClickEvent event) {
		Backpack backpack = this.backpackHandler.getBackpack(event.getClickedInventory());
		if (backpack == null) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		BackpackTransferPlayer transfer = backpack.getTransfer(player);
		ItemStack currentItem = event.getCurrentItem();

		switch (event.getAction()) {
		case SWAP_WITH_CURSOR:
			transfer.track(event.getCursor(), event.getCursor().getAmount());
			transfer.track(currentItem, -currentItem.getAmount());
			break;

		case HOTBAR_SWAP:
			int hotbar = event.getHotbarButton();
			if (hotbar == -1) {
				return;
			}

			ItemStack hotbarItem = player.getInventory().getItem(hotbar);
			transfer.track(hotbarItem, hotbarItem.getAmount());
			transfer.track(currentItem, -currentItem.getAmount());
			break;
		
		case PLACE_ALL:
		case PLACE_SOME:
		case PLACE_ONE:
			transfer.track(event.getCursor(), event.getCursor().getAmount());
			break;

		case PICKUP_ALL:
		case PICKUP_HALF:
		case PICKUP_ONE:
		case PICKUP_SOME:
		case COLLECT_TO_CURSOR:
		case DROP_ALL_SLOT:
		case DROP_ONE_SLOT:
			transfer.track(currentItem, -currentItem.getAmount());
			break;

		default:
			return;
		}
	}

	@EventHandler(ignoreCancelled = false)
	public void onInventoryClose(InventoryCloseEvent event) {
		Inventory topInventory = event.getPlayer().getOpenInventory().getTopInventory();
		if (topInventory != null) {
			Backpack backpack = this.backpackHandler.getBackpack(topInventory);
			if (backpack != null) {
				backpack.closeTransfer((Player) event.getPlayer());
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