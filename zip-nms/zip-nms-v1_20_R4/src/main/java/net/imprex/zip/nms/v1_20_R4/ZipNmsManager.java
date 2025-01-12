package net.imprex.zip.nms.v1_20_R4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.imprex.zip.common.ReflectionUtil;
import net.imprex.zip.nms.api.NmsManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class ZipNmsManager implements NmsManager {

	private static final Class<?> CRAFTMETASKULL_CLASS = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Method CRAFTMETASKULL_SET_PROFILE = ReflectionUtil.getMethod(CRAFTMETASKULL_CLASS,
			"setProfile", GameProfile.class);

	private static final RegistryAccess DEFAULT_REGISTRY = CraftRegistry.getMinecraftRegistry();

	private static final CompoundTag NBT_EMPTY_ITEMSTACK = new CompoundTag();

	static {
		NBT_EMPTY_ITEMSTACK.putString("id", "minecraft:air");
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
	public byte[] itemstackArrayToBinary(ItemStack[] items) {
		CompoundTag inventory = new CompoundTag();
		ListTag list = new ListTag();
		for (ItemStack itemStack : items) {
			if (itemStack == null || itemStack.getType() == Material.AIR) {
				list.add(NBT_EMPTY_ITEMSTACK);
			} else {
				net.minecraft.world.item.ItemStack craftItem = CraftItemStack.asNMSCopy(itemStack);
				Tag tag = craftItem.save(DEFAULT_REGISTRY);
				list.add(tag);
			}
		}
		inventory.put("i", list);
		return nbtToBinary(inventory);
	}

	@Override
	public List<ItemStack> binaryToItemStackArray(byte[] binary) {
		CompoundTag nbt = binaryToNBT(binary);
		List<ItemStack> items = new ArrayList<>();
		if (nbt.contains("i", 9)) {
			ListTag list = nbt.getList("i", 10);
			for (Tag base : list) {
				if (base instanceof CompoundTag itemTag) {
					if (itemTag.getString("id").equals("minecraft:air")) {
						items.add(new ItemStack(Material.AIR));
					} else {
						Optional<net.minecraft.world.item.ItemStack> optional = net.minecraft.world.item.ItemStack.parse(DEFAULT_REGISTRY, itemTag);
						if (optional.isPresent()) {
							items.add(CraftItemStack.asBukkitCopy(optional.get()));
						}
					}
				}
			}
		}
		return items;
	}

	@Override
	public byte[] itemstackToBinary(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return nbtToBinary(NBT_EMPTY_ITEMSTACK);
		} else {
			net.minecraft.world.item.ItemStack craftItem = CraftItemStack.asNMSCopy(item);
			CompoundTag tag = (CompoundTag) craftItem.save(DEFAULT_REGISTRY);
			return nbtToBinary(tag);
		}
	}

	@Override
	public ItemStack binaryToItemStack(byte[] binary) {
		CompoundTag nbt = binaryToNBT(binary);

		if (nbt.getString("id").equals("minecraft:air")) {
			return new ItemStack(Material.AIR);
		} else {
			Optional<net.minecraft.world.item.ItemStack> optional = net.minecraft.world.item.ItemStack.parse(DEFAULT_REGISTRY, nbt);
			if (optional.isPresent()) {
				return CraftItemStack.asBukkitCopy(optional.get());
			}
		}

		return null;
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