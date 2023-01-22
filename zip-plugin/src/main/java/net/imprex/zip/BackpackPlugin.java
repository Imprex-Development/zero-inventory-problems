package net.imprex.zip;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.imprex.zip.command.BackpackCommand;
import net.imprex.zip.config.BackpackConfig;
import net.imprex.zip.util.ZIPLogger;

public class BackpackPlugin extends JavaPlugin implements Listener {

	private NamespacedKey backpackIdentifierKey;
	private NamespacedKey backpackStorageKey;

	private BackpackConfig backpackConfig;
	private BackpackRegistry backpackRegistry;
	private BackpackHandler backpackHandler;

	private UpdateSystem updateSystem;

	@Override
	public void onLoad() {
		this.backpackIdentifierKey = this.createNamespacedKey("backpack.type");
		this.backpackStorageKey = this.createNamespacedKey("backpack.id");

		this.backpackConfig = new BackpackConfig(this);
		this.backpackRegistry = new BackpackRegistry(this);
		this.backpackHandler = new BackpackHandler(this);
	}

	@Override
	public void onEnable() {
		try {
			NmsInstance.initialize();

			this.backpackConfig.deserialize();

			this.backpackRegistry.register();
			this.backpackHandler.loadBackpacks();

			getCommand("zeroinventoryproblems").setExecutor(new BackpackCommand(this));

			this.updateSystem = new UpdateSystem(this);

			new MetricsSystem(this);

			Bukkit.getPluginManager().registerEvents(new BackpackListener(this), this);
		} catch (Exception e) {
			ZIPLogger.error("An error occured while enabling plugin", e);

			Bukkit.getPluginManager().registerEvents(this, this);
		}
	}

	@Override
	public void onDisable() {
		Bukkit.getOnlinePlayers().forEach(Player::closeInventory);

		if (this.backpackHandler != null) {
			this.backpackHandler.disable();
		}
		if (this.backpackRegistry != null) {
			this.backpackRegistry.unregister();
		}

		Bukkit.getServer().getScheduler().cancelTasks(this);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		this.handleItemOnError(event, event.getPlayer(), event.getItemInHand());
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		this.handleItemOnError(event, event.getPlayer(), event.getItem());
	}

	public void handleItemOnError(Cancellable event, Player player, ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (item == null || meta == null) {
			return;
		}

		PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
		if (persistentDataContainer.has(this.backpackIdentifierKey, PersistentDataType.STRING)) {
			event.setCancelled(true);

			player.sendMessage("""
					§8[§eZeroInventoryProblems§8] §7Item is §cdisabled §7because of a plugin error§8.
					§8[§eZeroInventoryProblems§8] §7Please contact your server administrator§8.
					""");
		}
	}

	public NamespacedKey createNamespacedKey(String key) {
		return new NamespacedKey(this, key);
	}

	public UpdateSystem getUpdateSystem() {
		return this.updateSystem;
	}

	public BackpackHandler getBackpackHandler() {
		return this.backpackHandler;
	}

	public BackpackRegistry getBackpackRegistry() {
		return this.backpackRegistry;
	}

	public BackpackConfig getBackpackConfig() {
		return this.backpackConfig;
	}

	public NamespacedKey getBackpackStorageKey() {
		return this.backpackStorageKey;
	}

	public NamespacedKey getBackpackIdentifierKey() {
		return this.backpackIdentifierKey;
	}
}