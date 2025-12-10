
package dev.imprex.zip;

import java.lang.reflect.Constructor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonObject;

import dev.imprex.zip.nms.api.ItemStackContainerResult;
import dev.imprex.zip.nms.api.NmsManager;
import dev.imprex.zip.common.MinecraftVersion;
import dev.imprex.zip.common.ServerVersion;
import dev.imprex.zip.common.ZIPLogger;

public class NmsInstance {
	
	private static NmsManager instance;

	public static void initialize() {

		if (NmsInstance.instance != null) {
			throw new IllegalStateException("NMS adapter is already initialized!");
		}

		String nmsVersion = MinecraftVersion.nmsVersion();
		if (ServerVersion.isMojangMapped()) {
			nmsVersion += "_mojang";
		}
		
		ZIPLogger.info("Searching NMS adapter for server version \"" + nmsVersion + "\"!");

		try {
			String className = "dev.imprex.zip.nms." + nmsVersion + ".ZipNmsManager";
			Class<? extends NmsManager> nmsManager = Class.forName(className).asSubclass(NmsManager.class);
			Constructor<? extends NmsManager> constructor = nmsManager.getConstructor();
			NmsInstance.instance = constructor.newInstance();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Server version \"" + nmsVersion + "\" is currently not supported!", e);
		} catch (Exception e) {
			throw new RuntimeException("Couldn't initialize NMS adapter", e);
		}

		ZIPLogger.info("NMS adapter for server version \"" + nmsVersion + "\" found!");
	}

	public static JsonObject itemstackToJsonElement(ItemStack[] items) {
		return instance.itemstackToJsonElement(items);
	}

	public static ItemStackContainerResult jsonElementToItemStack(JsonObject jsonElement) {
		return instance.jsonElementToItemStack(jsonElement);
	}

	public static JsonObject migrateToJsonElement(byte[] binary) {
		return instance.migrateToJsonElement(binary);
	}

	public static void setSkullProfile(SkullMeta meta, String texture) {
		instance.setSkullProfile(meta, texture);
	}

	public static boolean isAir(Material material) {
		return instance.isAir(material);
	}
}
