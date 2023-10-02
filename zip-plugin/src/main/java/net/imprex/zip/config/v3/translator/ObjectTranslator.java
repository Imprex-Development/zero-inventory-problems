package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleTranslator;

public class ObjectTranslator implements SimpleTranslator<Object, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleKey field, Object value) {
		config.set(field.value(), value);
	}

	@Override
	public Object deserialize(ConfigurationSection config, SimpleKey key) {
		return config.get(key.value(), null);
	}

	@Override
	public Class<Object> type() {
		return Object.class;
	}
}