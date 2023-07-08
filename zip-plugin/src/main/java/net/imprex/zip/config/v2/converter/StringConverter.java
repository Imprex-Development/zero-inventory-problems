package net.imprex.zip.config.v2.converter;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public class StringConverter implements ConfigConverter<String> {

	@Override
	public void serialize(ConfigurationSection section, ConfigEntry field, String value) {
		section.set(field.name(), value);
	}

	@Override
	public String deserialize(ConfigurationSection section, ConfigEntry field) {
		return section.getString(field.name(), field.defaultValue());
	}

	@Override
	public Class<?>[] getType() {
		return new Class<?>[] {
			String.class
		};
	}
}