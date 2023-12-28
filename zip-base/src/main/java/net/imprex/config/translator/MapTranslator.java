package net.imprex.config.translator;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;

@SuppressWarnings("rawtypes")
public class MapTranslator implements SimpleTranslator<Map, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Map value, Annotation requirement) {
		config.set(key.name(), value);
	}

	@Override
	public Map<?, ?> deserialize(ConfigurationSection config, SimpleTranslatorKey key, Map defaultValue, Annotation requirement) {
		ConfigurationSection section = config.getConfigurationSection(key.name());
		return section != null ? section.getValues(true) : defaultValue;
	}

	@Override
	public Map<?, ?> defaultValue(SimpleTranslatorKey key, Map initialValue, Annotation requirement) {
		return initialValue != null ? initialValue : new HashMap<>();
	}

	@Override
	public List<Class<? extends Map>> types() {
		return List.of(Map.class);
	}
}