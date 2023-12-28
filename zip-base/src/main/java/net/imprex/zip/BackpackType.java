package net.imprex.zip;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import net.imprex.zip.api.ZIPBackpackType;
import net.imprex.zip.config.BackpackConfig;
import net.imprex.zip.util.ItemFactory;

public class BackpackType implements ZIPBackpackType {

	private final BackpackPlugin plugin;
	private final BackpackConfig config;

	private final ItemStack item;
	private final BackpackRecipe recipe;

	public BackpackType(BackpackPlugin plugin, BackpackConfig config) {
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

	@Override
	public ItemStack createItem() {
		ItemStack item = new ItemStack(this.item);
		Backpack backpack = this.create();
		backpack.applyOnItem(item);
		return item;
	}

	@Override
	public Backpack create() {
		return new Backpack(this.plugin, this);
	}

	@Override
	public int getInventoryRows() {
		return this.config.inventoryRows;
	}

	@Override
	public String getUniqueName() {
		return this.config.uniqueName;
	}

	@Override
	public String getDisplayName() {
		return this.config.displayName;
	}

	@Override
	public ItemStack getItem() {
		return this.item;
	}

	@Override
	public BackpackRecipe getRecipe() {
		return this.recipe;
	}
}