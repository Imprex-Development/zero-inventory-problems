package dev.imprex.zip.nms.v1_19_R3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
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

import dev.imprex.zip.common.BPConstants;
import dev.imprex.zip.common.ReflectionUtil;
import dev.imprex.zip.nms.api.ItemStackContainerResult;
import dev.imprex.zip.nms.api.ItemStackWithSlot;
import dev.imprex.zip.nms.api.NmsManager;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;

public class ZipNmsManager extends NmsManager {

	private static final Class<?> CRAFTMETASKULL_CLASS = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Method CRAFTMETASKULL_SET_PROFILE = ReflectionUtil.getMethod(CRAFTMETASKULL_CLASS,
			"setProfile", GameProfile.class);

	private static final int DATA_VERSION = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

	@SuppressWarnings("deprecation")
	private static final RegistryAccess DEFAULT_REGISTRY = MinecraftServer.getServer().registryAccess();
	
	private static final DynamicOps<Tag> DYNAMIC_OPS_NBT = RegistryOps.create(NbtOps.INSTANCE, DEFAULT_REGISTRY);
	private static final DynamicOps<JsonElement> DYNAMIC_OPS_JSON = RegistryOps.create(JsonOps.INSTANCE, DEFAULT_REGISTRY);

	@Override
	public JsonObject itemstackToJsonElement(ItemStack[] items) {
		JsonArray jsonItems = new JsonArray();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			net.minecraft.world.item.ItemStack minecraftItem = asNmsCopy(item, net.minecraft.world.item.ItemStack.class);
			
			DataResult<JsonElement> result = net.minecraft.world.item.ItemStack.CODEC.encodeStart(DYNAMIC_OPS_JSON, minecraftItem);
			JsonObject resultJson = result.getOrThrow(false, error -> {}).getAsJsonObject();
			
			resultJson.addProperty(BPConstants.KEY_INVENTORY_SLOT, slot);
			jsonItems.add(resultJson);
		}
		
		JsonObject outputJson = new JsonObject();
		outputJson.addProperty(BPConstants.KEY_INVENTORY_VERSION, BPConstants.INVENTORY_VERSION);
		outputJson.addProperty(BPConstants.KEY_INVENTORY_DATA_VERSION, DATA_VERSION);
		outputJson.addProperty(BPConstants.KEY_INVENTORY_ITEMS_SIZE, items.length);
		outputJson.add(BPConstants.KEY_INVENTORY_ITEMS, jsonItems);
		return outputJson;
	}

	@Override
	public ItemStackContainerResult jsonElementToItemStack(JsonObject json) {
		// check if current version the same
		if (json.get(BPConstants.KEY_INVENTORY_VERSION).getAsInt() != BPConstants.INVENTORY_VERSION) {
			throw new IllegalStateException("Unable to convert binary to itemstack because zip version is missmatching");
		}
		
		int dataVersion = json.get(BPConstants.KEY_INVENTORY_DATA_VERSION).getAsInt();
		int itemsSize = json.get(BPConstants.KEY_INVENTORY_ITEMS_SIZE).getAsInt();
		
		List<ItemStackWithSlot> items = new ArrayList<>();
		
		JsonArray jsonItems = json.get(BPConstants.KEY_INVENTORY_ITEMS).getAsJsonArray();
		for (JsonElement item : jsonItems) {
			Dynamic<JsonElement> dynamicItem = new Dynamic<>(JsonOps.INSTANCE, item);
			Dynamic<JsonElement> dynamicItemFixed = DataFixers.getDataFixer()
					.update(References.ITEM_STACK, dynamicItem, dataVersion, DATA_VERSION);
			
			net.minecraft.world.item.ItemStack minecraftItem = net.minecraft.world.item.ItemStack.CODEC
					.parse(DYNAMIC_OPS_JSON, dynamicItemFixed.getValue())
					.getOrThrow(false, error -> {});
			
			ItemStack bukkitItem = asCraftMirror(minecraftItem);
			int slot = item.getAsJsonObject().get(BPConstants.KEY_INVENTORY_SLOT).getAsInt();
			
			items.add(new ItemStackWithSlot(slot, bukkitItem));
		}
		
		return new ItemStackContainerResult(itemsSize, items);
	}
	
	@Override
	public JsonObject migrateToJsonElement(byte[] binary) {
		CompoundTag compound;
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(binary)) {
			compound = NbtIo.readCompressed(inputStream);
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
						.getOrThrow(false, error -> {});
				
				DataResult<JsonElement> result = net.minecraft.world.item.ItemStack.CODEC.encodeStart(DYNAMIC_OPS_JSON, minecraftItem);
				JsonObject resultJson = result.getOrThrow(false, error -> {}).getAsJsonObject();
				
				resultJson.addProperty(BPConstants.KEY_INVENTORY_SLOT, currentSlot);
				jsonItems.add(resultJson);
				
				currentSlot++;
			}
		}
		
		JsonObject json = new JsonObject();
		json.addProperty(BPConstants.KEY_INVENTORY_VERSION, BPConstants.INVENTORY_VERSION);
		json.addProperty(BPConstants.KEY_INVENTORY_DATA_VERSION, DATA_VERSION);
		json.addProperty(BPConstants.KEY_INVENTORY_ITEMS_SIZE, list.size());
		json.add(BPConstants.KEY_INVENTORY_ITEMS, jsonItems);
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