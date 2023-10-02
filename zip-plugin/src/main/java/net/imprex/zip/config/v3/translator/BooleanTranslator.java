package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleTranslator;

public class BooleanTranslator implements SimpleTranslator<Boolean, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleKey field, Boolean value) {
		config.set(field.value(), value);
	}

	@Override
	public Boolean deserialize(ConfigurationSection config, SimpleKey key) {
		return config.getBoolean(key.value(), false);
	}

	@Override
	public Class<Boolean> type() {
		return Boolean.class;
	}
}