package net.imprex.zip.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {

	private static final String MINECRAFTSERVER_PATH = "net.minecraft";
	private static final String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit";

	public static String getMinecraftServer(String className) {
		return String.format("%s.%s", MINECRAFTSERVER_PATH, className);
	}

	public static String getCraftBukkit(String className) {
		return String.format("%s.%s.%s", CRAFTBUKKIT_PATH, MinecraftVersion.nmsVersion(), className);
	}

	public static Class<?> getMinecraftServerClass(String className) {
		try {
			return Class.forName(getMinecraftServer(className));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> getCraftBukkitClass(String className) {
		try {
			return Class.forName(getCraftBukkit(className));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setField(Object instance, String name, Object value) {
		setField(instance.getClass(), instance, name, value);
	}

	public static void setField(Class<?> clazz, Object instance, String name, Object value) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			field.set(instance, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Object getField(Object instance, String name) {
		return getField(instance.getClass(), instance, name);
	}

	public static Object getField(Class<?> clazz, Object obj, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Field getField(String className, String name) {
		try {
			return getField(Class.forName(className), name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method getMethod(String className, String name, Class<?>... parameterTypes) {
		try {
			return getMethod(Class.forName(className), name, parameterTypes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			Method method = clazz.getDeclaredMethod(name, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
