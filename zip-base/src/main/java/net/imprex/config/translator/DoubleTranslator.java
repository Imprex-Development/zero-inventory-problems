package net.imprex.config.translator;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;
import net.imprex.config.require.SimpleDouble;

public class DoubleTranslator implements SimpleTranslator<Double, SimpleDouble> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Double value, SimpleDouble requirement) {
		config.set(key.name(), value);
	}

	@Override
	public Double deserialize(ConfigurationSection config, SimpleTranslatorKey key, Double defaultValue, SimpleDouble requirement) {
		return config.getDouble(key.name(), defaultValue);
	}

	@Override
	public Double requirement(SimpleTranslatorKey key, Double value, SimpleDouble requirement) {
		return this.getOrDefault(requirement, value);
	}

	@Override
	public Double defaultValue(SimpleTranslatorKey key, Double initialValue, SimpleDouble requirement) {
		return requirement != null ? this.getOrDefault(requirement, null) : initialValue;
	}

	public double getOrDefault(SimpleDouble requirement, Double value) {
		if (value == null) {
			value = requirement.defaultValue();
		}

		double minValue = requirement.min();
		double maxValue = requirement.max();
		return value >= minValue ? value <= maxValue ? value : maxValue : minValue;
	}

	@Override
	public Class<SimpleDouble> require() {
		return SimpleDouble.class;
	}

	@Override
	public List<Class<? extends Double>> types() {
		return List.of(Double.class, double.class);
	}
}