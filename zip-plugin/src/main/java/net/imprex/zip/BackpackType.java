package net.imprex.zip;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import net.imprex.zip.config.BackpackTypeConfig;
import net.imprex.zip.util.ItemFactory;

public class BackpackType {

	private final BackpackPlugin plugin;
	private final BackpackTypeConfig config;

	private final ItemStack item;
	private final BackpackRecipe recipe;

	public BackpackType(BackpackPlugin plugin, BackpackTypeConfig config) {
		this.plugin = plugin;
		this.config = config;

		this.item = new ItemFactory(Material.PLAYER_HEAD)
				.setDisplayName(config.displayName)
				.setSkullProfile(config.texture)
				.setCustomModelData(config.customModelData)
				.setPersistentDataContainer(plugin.getBackpackIdentifierKey(), PersistentDataType.STRING, config.uniqueName)
				.setLore(config.lore)
				.build();
		this.recipe = new BackpackRecipe(plugin, config, this.item);
	}

	public Backpack create() {
		return new Backpack(this.plugin, this);
	}

	public int getInventoryRows() {
		return this.config.inventoryRows;
	}

	public String getUniqueName() {
		return this.config.uniqueName;
	}

	public String getDisplayName() {
		return this.config.displayName;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public BackpackRecipe getRecipe() {
		return this.recipe;
	}
}