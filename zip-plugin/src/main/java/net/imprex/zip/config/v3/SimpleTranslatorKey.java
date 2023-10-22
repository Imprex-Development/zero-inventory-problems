package net.imprex.zip.config.v3;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslatorKey {

	static SimpleTranslatorKey key(SimpleField<?> field) {
		SimpleKey key = field.getKey();
		if (key == null) {
			return null;
		}

		String value = key.value().trim();
		return () -> value.isBlank() ? field.getFieldName() : value;
	}

	String name();

	default boolean isVersionInRoot(ConfigurationSection config) {
		return this.name().equalsIgnoreCase(SimpleConfig.VERSION_FIELD) && config.getRoot().equals(config);
	}
}