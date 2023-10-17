package net.imprex.zip.config.v3.translator;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import net.imprex.zip.config.v3.SimpleTranslator;
import net.imprex.zip.config.v3.SimpleTranslatorKey;
import net.imprex.zip.config.v3.require.SimpleInteger;

public class IntegerTranslator implements SimpleTranslator<Integer, SimpleInteger> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, Integer value) {
		config.set(key.name(), value);
	}

	@Override
	public Integer deserialize(ConfigurationSection config, SimpleTranslatorKey key) {
		return config.getInt(key.name(), 0);
	}

	@Override
	public Integer requirement(SimpleTranslatorKey key, Integer value, SimpleInteger requirement) {
		return requirement.min() <= value
				&& requirement.max() >= value
				? value : requirement.defaultValue();
	}

	@Override
	public Integer defaultValue(SimpleTranslatorKey key, SimpleInteger requirement) {
		return requirement.defaultValue();
	}

	@Override
	public Class<SimpleInteger> requires() {
		return SimpleInteger.class;
	}

	@Override
	public List<Class<? extends Integer>> types() {
		return List.of(Integer.class, int.class);
	}
}