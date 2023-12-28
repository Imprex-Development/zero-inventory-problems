package net.imprex.config.translator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import net.imprex.config.SimpleTranslator;
import net.imprex.config.SimpleTranslatorKey;
import net.imprex.config.SimpleTranslatorRegistry;
import net.imprex.config.require.SimpleList;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListTranslator implements SimpleTranslator<List, SimpleList> {

	@Override
	public void serialize(ConfigurationSection config, SimpleTranslatorKey key, List value, SimpleList requirement) {
		YamlConfiguration configuration = new YamlConfiguration();
		SimpleTranslator translator = null;
		List<String> mappedList = new ArrayList<>();
		for (Object entry : value) {
			if (entry == null) {
				continue;
			}

			configuration.set(key.name(), entry);

			if (translator == null) {
				translator = SimpleTranslatorRegistry.getTranslator(entry.getClass());
			}
			translator.serialize(configuration, key, entry, null);
			Object parsed = configuration.get(key.name());
			if (parsed != null) {
				mappedList.add(parsed.toString());
			}
		}
		config.set(key.name(), mappedList);
	}

	@Override
	public List<?> deserialize(ConfigurationSection config, SimpleTranslatorKey key, List defaultValue, SimpleList requirement) {
		List<String> list = config.getStringList(key.name());
		if (list == null || requirement == null) {
			return defaultValue;
		}

		SimpleTranslator translator = SimpleTranslatorRegistry.getTranslator(requirement.typeClass());
		if (translator == null) {
			return defaultValue;
		}

		YamlConfiguration configuration = new YamlConfiguration();
		List mappedList = new ArrayList<>();
		for (String value : list) {
			if (value == null) {
				continue;
			}

			configuration.set(key.name(), value);
			Object parsed = translator.deserialize(configuration, key, null, null);
			if (parsed != null) {
				mappedList.add(parsed);
			}
		}
		return mappedList;
	}

	@Override
	public List<?> defaultValue(SimpleTranslatorKey key, List initialValue, SimpleList requirement) {
		return initialValue != null ? initialValue : new ArrayList<>();
	}

	@Override
	public Class<SimpleList> require() {
		return SimpleList.class;
	}

	@Override
	public List<Class<? extends List>> types() {
		return List.of(List.class);
	}
}