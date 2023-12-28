package net.imprex.config.translator;

import java.lang.annotation.Annotation;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;

@SuppressWarnings("rawtypes")
public class EnumTranslator implements SimpleTranslator<Enum, Annotation> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Enum value, Annotation requirement) {
		config.set(key.name(), value.name());
	}

	@Override
	public Enum deserialize(ConfigurationSection config, SimpleTranslatorKey key, Enum defaultValue, Annotation requirement) {
		if (defaultValue == null) {
			return defaultValue;
		}

		String enumName = config.getString(key.name());
		Enum[] values = defaultValue.getClass().getEnumConstants();
		for (Enum value : values) {
			if (value.name().equalsIgnoreCase(enumName)) {
				return value;
			}
		}
		return null;
	}

	@Override
	public Enum defaultValue(SimpleTranslatorKey key, Enum initialValue, Annotation requirement) {
		return initialValue != null ? initialValue : null;
	}

	@Override
	public List<Class<? extends Enum>> types() {
		return List.of(Enum.class);
	}
}