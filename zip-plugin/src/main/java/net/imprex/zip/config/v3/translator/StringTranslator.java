package net.imprex.zip.config.v3.translator;

import java.util.regex.Pattern;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.require.SimpleString;

public class StringTranslator implements SimpleTranslator<String, SimpleString> {

	@Override
	public void serialize(ConfigurationSection config, SimpleKey key, String value) {
		config.set(key.value(), value);
	}

	@Override
	public String deserialize(ConfigurationSection config, SimpleKey key) {
		return config.getString(key.value());
	}

	@Override
	public String requirement(SimpleKey key, String value, SimpleString requirement) {
		String patternString = requirement.regex();
		if (patternString.trim().isBlank()) {
			return value;
		}

		Pattern pattern = Pattern.compile(patternString);
		if (pattern.matcher(value).find()) {
			return value;
		}

		return requirement.defaultValue();
	}

	@Override
	public Class<SimpleString> requires() {
		return SimpleString.class;
	}

	@Override
	public Class<String> type() {
		return String.class;
	}
}