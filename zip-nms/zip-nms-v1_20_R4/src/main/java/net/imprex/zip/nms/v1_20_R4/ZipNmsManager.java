package net.imprex.zip.nms.v1_20_R4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.imprex.zip.common.BPKey;
import net.imprex.zip.common.ReflectionUtil;
import net.imprex.zip.common.ZIPLogger;
import net.imprex.zip.nms.api.NmsManager;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

public class ZipNmsManager implements NmsManager {

	private static final Class<?> CRAFTMETASKULL_CLASS = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Method CRAFTMETASKULL_SET_PROFILE = ReflectionUtil.getMethod(CRAFTMETASKULL_CLASS,
			"setProfile", GameProfile.class);

	private static final int DATA_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

	@SuppressWarnings("deprecation")
	private static final RegistryAccess DEFAULT_REGISTRY = MinecraftServer.getServer().registryAccess();

	private static final DynamicOps<Tag> DYNAMIC_OPS_NBT = DEFAULT_REGISTRY.createSerializationContext(NbtOps.INSTANCE);
	private static final DynamicOps<JsonElement> DYNAMIC_OPS_JSON = DEFAULT_REGISTRY.createSerializationContext(JsonOps.INSTANCE);

	@Override
	public JsonObject itemstackToJsonElement(ItemStack[] items) {
		JsonArray jsonItems = new JsonArray();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			net.minecraft.world.item.ItemStack minecraftItem = CraftItemStack.asNMSCopy(item);
			
			DataResult<JsonElement> result = net.minecraft.world.item.ItemStack.CODEC.encodeStart(DYNAMIC_OPS_JSON, minecraftItem);
			JsonObject resultJson = result.getOrThrow().getAsJsonObject();
			
			resultJson.addProperty(BPKey.INVENTORY_SLOT, slot);
			jsonItems.add(resultJson);
		}
		
		JsonObject outputJson = new JsonObject();
		outputJson.addProperty(BPKey.INVENTORY_VERSION, 2);
		outputJson.addProperty(BPKey.INVENTORY_DATA_VERSION, DATA_VERSION);
		outputJson.addProperty(BPKey.INVENTORY_ITEMS_SIZE, items.length);
		outputJson.add(BPKey.INVENTORY_ITEMS, jsonItems);
		return outputJson;
	}

	@Override
	public ItemStack[] jsonElementToItemStack(JsonObject json) {
		// check if current version the same
		if (json.get(BPKey.INVENTORY_VERSION).getAsInt() != 2) {
			throw new IllegalStateException("Unable to convert binary to itemstack because zip version is missmatching");
		}
		
		int dataVersion = json.get(BPKey.INVENTORY_DATA_VERSION).getAsInt();
		int itemsSize = json.get(BPKey.INVENTORY_ITEMS_SIZE).getAsInt();
		
		// convert json into bukkit item
		ItemStack[] items = new ItemStack[itemsSize];
		Arrays.fill(items, new ItemStack(Material.AIR));
		
		List<ItemStack> duplicateSlot = null;
		
		JsonArray jsonItems = json.get(BPKey.INVENTORY_ITEMS).getAsJsonArray();
		for (JsonElement item : jsonItems) {
			Dynamic<JsonElement> dynamicItem = new Dynamic<>(JsonOps.INSTANCE, item);
			Dynamic<JsonElement> dynamicItemFixed = DataFixers.getDataFixer()
					.update(References.ITEM_STACK, dynamicItem, dataVersion, DATA_VERSION);
			
			net.minecraft.world.item.ItemStack minecraftItem = net.minecraft.world.item.ItemStack.CODEC
					.parse(DYNAMIC_OPS_JSON, dynamicItemFixed.getValue())
					.getOrThrow();
			
			ItemStack bukkitItem = CraftItemStack.asCraftMirror(minecraftItem);
			int slot = item.getAsJsonObject().get(BPKey.INVENTORY_SLOT).getAsInt();
			
			if (itemsSize <= slot) {
				// something went wrong !? maybe user modified it him self
				ZIPLogger.warn("Slot size was extended from " + itemsSize + " to " + slot + " this should not happen. Do not change the slot number inside the config manually!?");
				
				ItemStack[] newItems = new ItemStack[slot + 1];
				System.arraycopy(items, 0, newItems, 0, items.length);
				Arrays.fill(newItems, items.length, newItems.length, new ItemStack(Material.AIR));
				items = newItems;
			}
			
			if (items[slot].getType() != Material.AIR) {
				if (duplicateSlot == null) {
					duplicateSlot = new ArrayList<>();
				}
				duplicateSlot.add(bukkitItem);
				ZIPLogger.warn("Duplicate item found on slot " + slot + " this should not happen. Do not change the slot number inside the config manually!?");
			} else {
				items[slot] = bukkitItem;
			}
		}
		
		// fill existing empty slots with duplicate item
		while (duplicateSlot != null && !duplicateSlot.isEmpty()) {
			outher: for (Iterator<ItemStack> iterator = duplicateSlot.iterator(); iterator.hasNext();) {
				ItemStack itemStack = (ItemStack) iterator.next();
				
				for (int i = 0; i < items.length; i++) {
					if (items[i].getType() == Material.AIR) {
						items[i] = itemStack;
						iterator.remove();
						break;
					} else if (i == items.length - 1) {
						break outher;
					}
				}
			}

			// extend slot limit and try again
			if (!duplicateSlot.isEmpty()) {
				int extendedSlots = items.length + duplicateSlot.size();
				ItemStack[] newItems = new ItemStack[extendedSlots];
				System.arraycopy(items, 0, newItems, 0, items.length);
				Arrays.fill(newItems, items.length, newItems.length, new ItemStack(Material.AIR));
				items = newItems;
			}
		}
		
		return items;
	}
	
	@Override
	public JsonObject migrateToJsonElement(byte[] binary) {
		CompoundTag compound;
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(binary)) {
			compound = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to parse binary to nbt", e);
		}
		
		ListTag list = compound.getList("i", 10);

		int currentSlot = 0;
		
		JsonArray jsonItems = new JsonArray();
		for (Tag base : list) {
			if (base instanceof CompoundTag itemTag) {
				String itemType = itemTag.getString("id");
				if (itemType.equals("minecraft:air")) {
					currentSlot++;
					continue;
				}
				
				Dynamic<Tag> dynamicItem = new Dynamic<>(NbtOps.INSTANCE, itemTag);
				net.minecraft.world.item.ItemStack minecraftItem = net.minecraft.world.item.ItemStack.CODEC
						.parse(DYNAMIC_OPS_NBT, dynamicItem.getValue())
						.getOrThrow();
				
				DataResult<JsonElement> result = net.minecraft.world.item.ItemStack.CODEC.encodeStart(DYNAMIC_OPS_JSON, minecraftItem);
				JsonObject resultJson = result.getOrThrow().getAsJsonObject();
				
				resultJson.addProperty(BPKey.INVENTORY_SLOT, currentSlot);
				jsonItems.add(resultJson);
				
				currentSlot++;
			}
		}
		
		JsonObject json = new JsonObject();
		json.addProperty(BPKey.INVENTORY_VERSION, 2);
		json.addProperty(BPKey.INVENTORY_DATA_VERSION, DATA_VERSION);
		json.addProperty(BPKey.INVENTORY_ITEMS_SIZE, list.size());
		json.add(BPKey.INVENTORY_ITEMS, jsonItems);
		return json;
	}

	@Override
	public void setSkullProfile(SkullMeta meta, String texture) {
		try {
			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
			gameProfile.getProperties().put("textures", new Property("textures", texture));
			CRAFTMETASKULL_SET_PROFILE.invoke(meta, gameProfile);
		} catch (Exception e) {
			throw new ClassCastException("Error by setting skull profile");
		}
	}

	@Override
	public boolean isAir(Material material) {
		return material == null || material == Material.AIR;
	}
}