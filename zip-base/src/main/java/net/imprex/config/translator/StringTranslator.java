package net.imprex.config.translator;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;
import net.imprex.config.require.SimpleString;

public class StringTranslator implements SimpleTranslator<String, SimpleString> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, String value, SimpleString requirement) {
		config.set(key.name(), value);
	}

	@Override
	public String deserialize(ConfigurationSection config, SimpleTranslatorKey key, String defaultValue, SimpleString requirement) {
		return config.getString(key.name(), defaultValue);
	}

	@Override
	public String requirement(SimpleTranslatorKey key, String value, SimpleString requirement) {
		String patternString = requirement.regex();
		if (patternString.trim().isBlank()) {
			return value;
		}

		if (Pattern.matches(patternString, value)) {
			return value;
		}

		return requirement.defaultValue();
	}

	@Override
	public String defaultValue(SimpleTranslatorKey key, String initialValue, SimpleString requirement) {
		return requirement != null ? requirement.defaultValue() : initialValue;
	}

	@Override
	public Class<SimpleString> require() {
		return SimpleString.class;
	}

	@Override
	public List<Class<? extends String>> types() {
		return List.of(String.class);
	}
}