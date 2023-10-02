package net.imprex.zip.config.v3.translator;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.require.SimpleInteger;

public class IntegerTranslator implements SimpleTranslator<Integer, SimpleInteger> {

	@Override
	public void serialize(ConfigurationSection config, SimpleKey field, Integer value) {
		config.set(field.value(), value);
	}

	@Override
	public Integer deserialize(ConfigurationSection config, SimpleKey key) {
		return config.getInt(key.value(), 0);
	}

	@Override
	public Integer requirement(SimpleKey key, Integer value, SimpleInteger requirement) {
		return requirement.min() <= value
				&& requirement.max() >= value
				? value : requirement.defaultValue();
	}

	@Override
	public Class<SimpleInteger> requires() {
		return SimpleInteger.class;
	}

	@Override
	public Class<Integer> type() {
		return Integer.class;
	}
}