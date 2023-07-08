package net.imprex.zip.config.v2.converter;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public class ObjectConverter implements ConfigConverter<Object> {

	@Override
	public void serialize(ConfigurationSection section, ConfigEntry field, Object value) {
		section.set(field.name(), value);
	}

	@Override
	public Object deserialize(ConfigurationSection section, ConfigEntry field) {
		return section.get(field.name(), null);
	}

	@Override
	public Class<?>[] getType() {
		return new Class<?>[] {
			Object.class
		};
	}
}