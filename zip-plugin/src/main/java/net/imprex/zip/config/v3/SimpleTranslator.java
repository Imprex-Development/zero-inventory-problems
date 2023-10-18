package net.imprex.zip.config.v3;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslator<Value, Requirement extends Annotation> {

	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Value value);

	public Value deserialize(ConfigurationSection config, SimpleTranslatorKey key, Value defaultValue);

	public Value defaultValue(SimpleTranslatorKey key, Value initialValue, Requirement requirement);

	public default Value requirement(SimpleTranslatorKey key, Value value, Requirement requirement) {
		return null;
	}
	public default Class<Requirement> require() {
		return null;
	}

	public List<Class<? extends Value>> types();
}