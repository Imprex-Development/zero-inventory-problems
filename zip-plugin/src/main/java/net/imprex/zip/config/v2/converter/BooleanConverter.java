package net.imprex.zip.config.v2.converter;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public class BooleanConverter implements ConfigConverter<Boolean> {

	@Override
	public void serialize(ConfigurationSection section, ConfigEntry field, Boolean value) {
		section.set(field.name(), value != null ? value : Boolean.valueOf(field.defaultValue()));
	}

	@Override
	public Boolean deserialize(ConfigurationSection section, ConfigEntry field) {
		return section.getBoolean(field.name(), Boolean.valueOf(field.defaultValue()));
	}

	@Override
	public Class<?>[] getType() {
		return new Class<?>[] {
			Boolean.class
		};
	}
}