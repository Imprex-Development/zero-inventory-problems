package net.imprex.config;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

public interface SimpleTranslator<Value, Requirement extends Annotation> {

	void serialize(ConfigurationSection config, SimpleTranslatorKey key, Value value, Requirement requirement);

	Value deserialize(ConfigurationSection config, SimpleTranslatorKey key, Value defaultValue, Requirement requirement);

	Value defaultValue(SimpleTranslatorKey key, Value initialValue, Requirement requirement);

	default Value requirement(SimpleTranslatorKey key, Value value, Requirement requirement) {
		return null;
	}
	default Class<Requirement> require() {
		return null;
	}

	List<Class<? extends Value>> types();
}