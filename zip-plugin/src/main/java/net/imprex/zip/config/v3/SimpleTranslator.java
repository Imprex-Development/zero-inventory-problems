package net.imprex.zip.config.v3;

import java.lang.annotation.Annotation;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslator<Value, Requirement extends Annotation> {

	public void serialize(ConfigurationSection config, SimpleKey key, Value value);

	public Value deserialize(ConfigurationSection config, SimpleKey key);

	public default Value requirement(SimpleKey key, Value value, Requirement requirement) {
		return null;
	}

	public default Class<Requirement> requires() {
		return null;
	}

	public Class<? extends Value> type();
}