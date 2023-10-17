package net.imprex.zip.config.v3.translator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.SimpleTranslatorKey;

public class ListTranslator implements SimpleTranslator<List<?>, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, List<?> value) {
		config.set(key.name(), value);
	}

	@Override
	public List<?> deserialize(ConfigurationSection config, SimpleTranslatorKey key) {
		return config.getList(key.name(), new ArrayList<>());
	}

	@Override
	public List<?> defaultValue(SimpleTranslatorKey key, Annotation requirement) {
		return new ArrayList<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Class<? extends List<?>>> types() {
		return List.of((Class<? extends List<?>>) List.class);
	}
}