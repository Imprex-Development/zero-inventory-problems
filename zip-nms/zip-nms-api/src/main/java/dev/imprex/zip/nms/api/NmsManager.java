package dev.imprex.zip.nms.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;

import dev.imprex.zip.common.ReflectionUtil;

public abstract class NmsManager {
	
	private static final Class<?> CRAFT_ITEM_STACK_CLASS;

	private static Method craftItemStackAsNmsCopy;
	private static Method craftItemStackAsCraftMirror;
	
	static {
		try {
			CRAFT_ITEM_STACK_CLASS = Class.forName(Bukkit.getServer().getClass().getPackageName() + ".inventory.CraftItemStack");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected static <T> T asNmsCopy(ItemStack bukkitItem, Class<T> minecraftItemClass) {
		try {
			if (craftItemStackAsNmsCopy == null) {
				Method method = ReflectionUtil.searchMethod(
						CRAFT_ITEM_STACK_CLASS,
						minecraftItemClass,
						ItemStack.class);
				method.setAccessible(true);
				craftItemStackAsNmsCopy = method;
			}
			
			return minecraftItemClass.cast(craftItemStackAsNmsCopy.invoke(null, bukkitItem));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected static <T> ItemStack asCraftMirror(T minecraftItem) {
		try {
			if (craftItemStackAsCraftMirror == null) {
				Method method = ReflectionUtil.searchMethod(
						CRAFT_ITEM_STACK_CLASS,
						CRAFT_ITEM_STACK_CLASS,
						minecraftItem.getClass());
				method.setAccessible(true);
				craftItemStackAsCraftMirror = method;
			}
			
			return (ItemStack) craftItemStackAsCraftMirror.invoke(null, minecraftItem);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public abstract JsonObject itemstackToJsonElement(ItemStack[] items);

	public abstract ItemStackContainerResult jsonElementToItemStack(JsonObject jsonElement);
	
	public abstract JsonObject migrateToJsonElement(byte[] binary);

	public abstract void setSkullProfile(SkullMeta meta, String texture);

	public abstract boolean isAir(Material material);
}