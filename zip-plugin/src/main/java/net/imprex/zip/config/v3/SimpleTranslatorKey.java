package net.imprex.zip.config.v3;

import java.lang.reflect.Field;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslatorKey {

	static SimpleTranslatorKey key(Field field) {
		SimpleKey key = field.getAnnotation(SimpleKey.class);
		if (key == null) {
			return null;
		}

		String value = key.value().trim();
		return () -> value.isBlank() ? field.getName() : value;
	}

	String name();

	default boolean isVersionInRoot(ConfigurationSection config) {
		return this.name().equalsIgnoreCase(SimpleConfig.VERSION_FIELD) && config.getRoot().equals(config);
	}
}