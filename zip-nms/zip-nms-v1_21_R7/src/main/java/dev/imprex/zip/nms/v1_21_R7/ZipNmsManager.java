package dev.imprex.zip.nms.v1_21_R7;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Multimaps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.component.ResolvableProfile;

public class ZipNmsManager extends NmsManager {

	private static final int DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();

	@SuppressWarnings("deprecation")
	private static final RegistryAccess DEFAULT_REGISTRY = MinecraftServer.getServer().registryAccess();

	private static final DynamicOps<Tag> DYNAMIC_OPS_NBT = DEFAULT_REGISTRY.createSerializationContext(NbtOps.INSTANCE);
	private static final DynamicOps<JsonElement> DYNAMIC_OPS_JSON = DEFAULT_REGISTRY.createSerializationContext(JsonOps.INSTANCE);
	
	private static final BiConsumer<SkullMeta, GameProfile> SET_PROFILE;

	static {
		BiConsumer<SkullMeta, GameProfile> setProfile = (meta, profile) -> {
			throw new NullPointerException("Unable to find 'setProfile' method!");
		};

		Class<?> craftMetaSkullClass = new ItemStack(Material.PLAYER_HEAD)
				.getItemMeta()
				.getClass();

		Method setResolvableProfileMethod = ReflectionUtil.searchMethod(craftMetaSkullClass, void.class, ResolvableProfile.class);
		if (setResolvableProfileMethod != null) {
			setProfile = (meta, profile) -> {
				try {
					setResolvableProfileMethod.invoke(meta, ResolvableProfile.createResolved(profile));
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			};
		} else {
			Method setProfileMethod = ReflectionUtil.searchMethod(craftMetaSkullClass, void.class, GameProfile.class);
			if (setProfileMethod != null) {
				setProfile = (meta, profile) -> {
					try {
						setProfileMethod.invoke(meta, profile);
					} catch (IllegalAccessException | InvocationTargetException e) {
						e.printStackTrace();
					}
				};
			}
		}

		SET_PROFILE = setProfile;
	}
	
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
			JsonObject resultJson = result.getOrThrow().getAsJsonObject();
			
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
					.getOrThrow();
			
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
			compound = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
		} catch (IOException e) {
			throw new IllegalStateException("Unable to parse binary to nbt", e);
		}
		
		ListTag list = compound.getListOrEmpty("i");

		int currentSlot = 0;
		
		JsonArray jsonItems = new JsonArray();
		for (Tag base : list) {
			if (base instanceof CompoundTag itemTag) {
				String itemType = itemTag.getString("id").orElse("");
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
			HashMap<String, Property> properties = new HashMap<>();
			properties.put("textures", new Property("textures", texture));
			
			PropertyMap propertyMap = new PropertyMap(Multimaps.forMap(properties));
			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "", propertyMap);

			SET_PROFILE.accept(meta, gameProfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isAir(Material material) {
		return material == null || material == Material.AIR;
	}
}