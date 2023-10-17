package net.imprex.zip.config.v3;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslator<Value, Requirement extends Annotation> {

	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Value value);

	public Value deserialize(ConfigurationSection config, SimpleTranslatorKey key);

	public default Value requirement(SimpleTranslatorKey key, Value value, Requirement requirement) {
		return null;
	}

	public Value defaultValue(SimpleTranslatorKey key, Requirement requirement);

	public default Class<Requirement> requires() {
		return null;
	}

	public List<Class<? extends Value>> types();
}