package net.imprex.zip.nms.v1_20_R2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.imprex.zip.nms.api.NmsManager;
import net.imprex.zip.util.ReflectionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class ZipNmsManager implements NmsManager {

	private static final Class<?> CRAFTMETASKULL_CLASS = ReflectionUtil.getCraftBukkitClass("inventory.CraftMetaSkull");
	private static final Method CRAFTMETASKULL_SET_PROFILE = ReflectionUtil.getMethod(CRAFTMETASKULL_CLASS,
			"setProfile", GameProfile.class);

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
			return NbtIo.readCompressed(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CompoundTag();
	}

	@Override
	public byte[] itemstackToBinary(ItemStack[] items) {
		CompoundTag inventory = new CompoundTag();
		ListTag list = new ListTag();
		for (ItemStack itemStack : items) {
			net.minecraft.world.item.ItemStack craftItem = CraftItemStack.asNMSCopy(itemStack);
			list.add(craftItem.save(new CompoundTag()));
		}
		inventory.put("i", list);
		return nbtToBinary(inventory);
	}

	@Override
	public List<ItemStack> binaryToItemStack(byte[] binary) {
		CompoundTag nbt = binaryToNBT(binary);
		List<ItemStack> items = new ArrayList<>();
		if (nbt.contains("i", 9)) {
			ListTag list = nbt.getList("i", 10);
			for (Tag base : list) {
				if (base instanceof CompoundTag) {
					items.add(CraftItemStack.asBukkitCopy(net.minecraft.world.item.ItemStack.of((CompoundTag) base)));
				}
			}
		}
		return items;
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
	public void setupBackpackItem(ItemStack item) {
	}

	@Override
	public boolean isAir(Material material) {
		return material == null || material == Material.AIR;
	}
}