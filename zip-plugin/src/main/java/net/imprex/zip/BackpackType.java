package net.imprex.zip;

import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import net.imprex.zip.api.ZIPBackpackType;
import net.imprex.zip.config.BackpackTypeConfig;
import net.imprex.zip.util.ItemFactory;

public class BackpackType implements ZIPBackpackType {

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

	@Override
	public ItemStack createItem() {
		ItemStack item = new ItemStack(this.item);
		Backpack backpack = this.create();
		backpack.applyOnItem(item);
		return item;
	}

	@Override
	public Backpack create() {
		return new Backpack(this.plugin, this, null);
	}
	
	@Override
	public void updateItem(ItemStack item) {
		if (item != null && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(this.config.displayName);
			meta.setLore(this.config.lore);
			meta.setCustomModelData(this.config.customModelData);

			if (meta instanceof SkullMeta skullMeta) {
				NmsInstance.setSkullProfile(skullMeta, this.config.texture);
			}
			item.setItemMeta(meta);
		}
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
	public String getItemTexture() {
		return this.config.texture;
	}
	
	@Override
	public List<String> getLore() {
		return Collections.unmodifiableList(this.config.lore);
	}

	@Override
	public int getCustomModelData() {
		return this.config.customModelData;
	}

	@Override
	public ItemStack getItem() {
		return this.item;
	}

	@Override
	public BackpackRecipe getRecipe() {
		return this.recipe;
	}

	@Override
	public boolean hasCraftingPermission() {
		return this.config.craftingPermission != null;
	}

	@Override
	public String getCraftingPermission() {
		return this.config.craftingPermission;
	}
}