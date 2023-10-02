package net.imprex.zip.config.v2.converter;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v2.ConfigEntry;

public interface ConfigConverter<Value> {

	public void serialize(ConfigurationSection section, ConfigEntry field, Value value);

	public Value deserialize(ConfigurationSection section, ConfigEntry field);

	public Class<?>[] getType();
}