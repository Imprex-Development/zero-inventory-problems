
package net.imprex.zip;

import java.lang.reflect.Constructor;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.imprex.zip.common.MinecraftVersion;
import net.imprex.zip.nms.api.NmsManager;
import net.imprex.zip.util.ZIPLogger;

public class NmsInstance {

	private static NmsManager instance;

	public static void initialize() {
		if (NmsInstance.instance != null) {
			throw new IllegalStateException("NMS adapter is already initialized!");
		}

		String nmsVersion = MinecraftVersion.nmsVersion();
		ZIPLogger.info("Searching NMS adapter for server version \"" + nmsVersion + "\"!");

		try {
			String className = "net.imprex.zip.nms." + nmsVersion + ".ZipNmsManager";
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

	public static byte[] itemstackToBinary(ItemStack[] items) {
		return instance.itemstackToBinary(items);
	}

	public static List<ItemStack> binaryToItemStack(byte[] binary) {
		return instance.binaryToItemStack(binary);
	}

	public static void setSkullProfile(SkullMeta meta, String texture) {
		instance.setSkullProfile(meta, texture);
	}

	public static boolean isAir(Material material) {
		return instance.isAir(material);
	}
}
