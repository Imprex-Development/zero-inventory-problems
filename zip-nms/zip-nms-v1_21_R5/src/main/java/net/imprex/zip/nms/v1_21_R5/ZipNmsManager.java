package net.imprex.zip.nms.v1_21_R5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R5.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.imprex.zip.common.ReflectionUtil;
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
import net.minecraft.world.item.component.ResolvableProfile;

public class ZipNmsManager implements NmsManager {

	private static final BiConsumer<SkullMeta, GameProfile> SET_PROFILE;

	@SuppressWarnings("deprecation")
	private static final RegistryAccess DEFAULT_REGISTRY = MinecraftServer.getServer().registryAccess();

	private static final CompoundTag NBT_EMPTY_ITEMSTACK = new CompoundTag();
	
	private static final int DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();
	
	private static final Gson GSON = new Gson();

	private static final DynamicOps<Tag> DYNAMIC_OPS_NBT = DEFAULT_REGISTRY.createSerializationContext(NbtOps.INSTANCE);
	private static final DynamicOps<JsonElement> DYNAMIC_OPS_JSON = DEFAULT_REGISTRY.createSerializationContext(JsonOps.INSTANCE);
	
	static {
		NBT_EMPTY_ITEMSTACK.putString("id", "minecraft:air");

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
					setResolvableProfileMethod.invoke(meta, new ResolvableProfile(profile));
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

	public byte[] nbtToBinary(CompoundTag compound) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			NbtIo.writeCompressed(compound, outputStream);
			return outputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CompoundTag binaryToNBT(byte[] binary) {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(binary)) {
			return NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CompoundTag();
	}

	@Override
	public byte[] itemstackToBinary(ItemStack[] items) {
		JsonArray jsonItems = new JsonArray();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			net.minecraft.world.item.ItemStack minecraftItem = CraftItemStack.asNMSCopy(item);
			
			DataResult<JsonElement> result = net.minecraft.world.item.ItemStack.CODEC.encodeStart(DYNAMIC_OPS_JSON, minecraftItem);
			JsonObject resultJson = result.getOrThrow().getAsJsonObject();
			
			resultJson.addProperty("Slot", slot);
			jsonItems.add(resultJson);
		}
		
		JsonObject outputJson = new JsonObject();
		outputJson.addProperty("ZIPVersion", 2);
		outputJson.addProperty("DataVersion", DATA_VERSION);
		outputJson.addProperty("ContainerSize", items.length);
		outputJson.add("Items", jsonItems);
		
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			GSON.toJson(outputJson, outputStreamWriter);
			return outputStream.toByteArray();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to convert ItemStack into json", e);
		}
	}

	@Override
	public List<ItemStack> binaryToItemStack(byte[] binary) {
		try {
			// parse new version (JSON)
			JsonObject inputJson;
			try (ByteArrayInputStream inputStream = new ByteArrayInputStream(binary);
					InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
				inputJson = GSON.fromJson(inputStreamReader, JsonObject.class);
			}
			
			// check if current version the same
			if (inputJson.get("ZIPVersion").getAsInt() != 2) {
				throw new IllegalStateException("Unable to convert binary to itemstack because zip version is missmatching");
			}
			
			int dataVersion = inputJson.get("DataVersion").getAsInt();
			int containerSize = inputJson.get("ContainerSize").getAsInt();
			
			// convert json into bukkit item
			List<ItemStack> items = new ArrayList<>(containerSize);
			JsonArray jsonItems = inputJson.get("Items").getAsJsonArray();
			for (JsonElement item : jsonItems) {
				Dynamic<JsonElement> dynamicItem = new Dynamic<>(JsonOps.INSTANCE, item);
				Dynamic<JsonElement> dynamicItemFixed = DataFixers.getDataFixer().update(References.ITEM_STACK, dynamicItem, dataVersion, DATA_VERSION);
				net.minecraft.world.item.ItemStack minecraftItem = net.minecraft.world.item.ItemStack.CODEC
						.parse(DYNAMIC_OPS_JSON, dynamicItemFixed.getValue())
						.getOrThrow();
				
				ItemStack bukkitItem = CraftItemStack.asCraftMirror(minecraftItem);
				int slot = item.getAsJsonObject().get("Slot").getAsInt();
				items.set(slot, bukkitItem);
			}

			return items;
		} catch (Exception e) {
			// parse outdated version (NBT)
			CompoundTag compound = binaryToNBT(binary);
			ListTag list = compound.getListOrEmpty("i");
			if (list.isEmpty()) {
				return Collections.emptyList();
			}

			List<ItemStack> items = new ArrayList<>();
			for (Tag base : list) {
				if (base instanceof CompoundTag itemTag) {
					String itemType = itemTag.getString("id").orElse("");
					if (itemType.equals("minecraft:air")) {
						items.add(new ItemStack(Material.AIR));
					} else {
						Dynamic<Tag> dynamicItem = new Dynamic<>(NbtOps.INSTANCE, itemTag);
						net.minecraft.world.item.ItemStack minecraftItem = net.minecraft.world.item.ItemStack.CODEC
								.parse(DYNAMIC_OPS_NBT, dynamicItem.getValue())
								.getOrThrow();
						
						ItemStack bukkitItem = CraftItemStack.asCraftMirror(minecraftItem);
						items.add(bukkitItem);
					}
				}
			}
			return items;
		}
	}

	@Override
	public void setSkullProfile(SkullMeta meta, String texture) {
		try {
			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
			gameProfile.getProperties().put("textures", new Property("textures", texture));

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