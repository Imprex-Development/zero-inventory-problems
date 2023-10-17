package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.SimpleTranslatorKey;

public class ObjectTranslator implements SimpleTranslator<Object, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Object value) {
		config.set(key.name(), value);
	}

	@Override
	public Object deserialize(ConfigurationSection config, SimpleTranslatorKey key) {
		return config.get(key.name(), null);
	}

	@Override
	public Object defaultValue(SimpleTranslatorKey key, Annotation requirement) {
		return null;
	}

	@Override
	public List<Class<? extends Object>> types() {
		return List.of(Object.class);
	}
}