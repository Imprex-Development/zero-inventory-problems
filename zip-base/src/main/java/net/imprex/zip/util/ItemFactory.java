package net.imprex.zip.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import net.imprex.zip.NmsInstance;

public class ItemFactory extends ItemStack {

	private ItemMeta meta;

	public ItemFactory(Material material) {
		super(material);
		this.meta = this.getItemMeta();
	}

	public ItemFactory setDisplayName(String name) {
		this.meta.setDisplayName(name);
		return this;
	}

	public ItemFactory setLore(int index, String lore) {
		List<String> lores = this.meta.getLore();
		if (lores.size() > index) {
			lores.set(index, lore);
		}
		this.setLore(lores);
		return this;
	}

	public ItemFactory setLore(List<String> lore) {
		this.meta.setLore(lore);
		return this;
	}

	public ItemFactory setLore(String... lore) {
		this.meta.setLore(Arrays.asList(lore));
		return this;
	}

	public ItemFactory addLore(String lore) {
		List<String> temp = this.meta.getLore() != null ? this.meta.getLore() : new ArrayList<String>();
		temp.add(lore);
		this.meta.setLore(temp);
		return this;
	}

	public ItemFactory addLore(String... lores) {
		List<String> temp = this.meta.getLore() != null ? this.meta.getLore() : new ArrayList<String>();
		for (String lore : lores) {
			temp.add(lore);
		}
		this.meta.setLore(temp);
		return this;
	}

	public ItemFactory addAllItemFlag() {
		ItemMeta meta = this.meta;
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		return this;
	}

	public ItemFactory setSkullProfile(String texture) {
		NmsInstance.setSkullProfile((SkullMeta) this.meta, texture);
		return this;
	}

	public ItemFactory setCustomModelData(Integer data) {
		this.meta.setCustomModelData(data);
		return this;
	}

	public <Type> ItemFactory setPersistentDataContainer(NamespacedKey key, PersistentDataType<Type, Type> type, Type value) {
		this.meta.getPersistentDataContainer().set(key, type, value);
		return this;
	}

	public ItemStack build() {
		this.setItemMeta(this.meta);
		return this;
	}
}
