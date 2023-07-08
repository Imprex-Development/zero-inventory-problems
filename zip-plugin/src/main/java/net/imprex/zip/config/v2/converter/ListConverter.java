package net.imprex.zip.config.v2.converter;

import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public class ListConverter implements ConfigConverter<List<?>> {

	@Override
	public void serialize(ConfigurationSection section, ConfigEntry field, List<?> value) {
		section.set(field.name(), value);
	}

	@Override
	public List<?> deserialize(ConfigurationSection section, ConfigEntry field) {
		return section.getList(field.name(), Collections.emptyList());
	}

	@Override
	public Class<?>[] getType() {
		return new Class<?>[] {
			List.class
		};
	}
}