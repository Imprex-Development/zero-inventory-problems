package net.imprex.zip.config.v2.converter;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public class IntegerConverter implements ConfigConverter<Integer> {

	@Override
	public void serialize(ConfigurationSection section, ConfigEntry field, Integer value) {
		section.set(field.name(), value);
	}

	@Override
	public Integer deserialize(ConfigurationSection section, ConfigEntry field) {
		return section.getInt(field.name(), Integer.parseInt(field.defaultValue()));
	}

	@Override
	public Class<?>[] getType() {
		return new Class<?>[] {
			Integer.class,
			int.class
		};
	}
}