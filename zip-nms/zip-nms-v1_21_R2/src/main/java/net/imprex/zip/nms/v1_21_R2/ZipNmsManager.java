package net.imprex.zip.nms.v1_21_R2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R2.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
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
import net.minecraft.world.item.component.ResolvableProfile;

public class ZipNmsManager implements NmsManager {

	private static final BiConsumer<SkullMeta, GameProfile> SET_PROFILE;

	private static final RegistryAccess DEFAULT_REGISTRY = CraftRegistry.getMinecraftRegistry();

	private static final CompoundTag NBT_EMPTY_ITEMSTACK = new CompoundTag();

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
	public List<ItemStack> binaryToItemStack(byte[] binary) {
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