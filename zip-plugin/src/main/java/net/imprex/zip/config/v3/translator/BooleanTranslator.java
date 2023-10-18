package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.SimpleTranslatorKey;

public class BooleanTranslator implements SimpleTranslator<Boolean, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Boolean value) {
		config.set(key.name(), value);
	}

	@Override
	public Boolean deserialize(ConfigurationSection config, SimpleTranslatorKey key, Boolean defaultValue) {
		return config.getBoolean(key.name(), defaultValue);
	}

	@Override
	public Boolean defaultValue(SimpleTranslatorKey key, Boolean initialValue, Annotation requirement) {
		return initialValue != null ? initialValue : false;
	}

	@Override
	public List<Class<? extends Boolean>> types() {
		return List.of(Boolean.class, boolean.class);
	}
}