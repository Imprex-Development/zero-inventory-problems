package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleTranslator;

public class ListTranslator implements SimpleTranslator<List<?>, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleKey field, List<?> value) {
		config.set(field.value(), value);
	}

	@Override
	public List<?> deserialize(ConfigurationSection config, SimpleKey key) {
		return config.getList(key.value(), Collections.emptyList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends List<?>> type() {
		return (Class<? extends List<?>>) List.class;
	}
}