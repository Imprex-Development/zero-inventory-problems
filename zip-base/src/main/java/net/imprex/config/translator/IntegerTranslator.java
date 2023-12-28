package net.imprex.config.translator;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;
import net.imprex.config.require.SimpleInteger;

public class IntegerTranslator implements SimpleTranslator<Integer, SimpleInteger> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Integer value, SimpleInteger requirement) {
		config.set(key.name(), value);
	}

	@Override
	public Integer deserialize(ConfigurationSection config, SimpleTranslatorKey key, Integer defaultValue, SimpleInteger requirement) {
		return config.getInt(key.name(), defaultValue);
	}

	@Override
	public Integer requirement(SimpleTranslatorKey key, Integer value, SimpleInteger requirement) {
		return this.getOrDefault(requirement, value);
	}

	@Override
	public Integer defaultValue(SimpleTranslatorKey key, Integer initialValue, SimpleInteger requirement) {
		return requirement != null ? this.getOrDefault(requirement, null) : initialValue;
	}

	public int getOrDefault(SimpleInteger requirement, Integer value) {
		if (value == null) {
			value = requirement.defaultValue();
		}

		int minValue = requirement.min();
		int maxValue = requirement.max();
		return value >= minValue ? value <= maxValue ? value : maxValue : minValue;
	}

	@Override
	public Class<SimpleInteger> require() {
		return SimpleInteger.class;
	}

	@Override
	public List<Class<? extends Integer>> types() {
		return List.of(Integer.class, int.class);
	}
}