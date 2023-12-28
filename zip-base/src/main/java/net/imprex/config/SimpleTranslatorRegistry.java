package net.imprex.config;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.imprex.config.translator.BooleanTranslator;
import net.imprex.config.translator.DoubleTranslator;
import net.imprex.config.translator.EnumTranslator;
import net.imprex.config.translator.IntegerTranslator;
import net.imprex.config.translator.ListTranslator;
import net.imprex.config.translator.MapTranslator;
import net.imprex.config.translator.ObjectTranslator;
import net.imprex.config.translator.StringTranslator;

public class SimpleTranslatorRegistry {

	private static final Map<Class<?>, SimpleTranslator<?, ?>> TRANSLATOR_LIST = new HashMap<>();
	private static final ObjectTranslator DEFAULT_TRANSLATOR = new ObjectTranslator();

	static {
		registerTranslator(new BooleanTranslator());
		registerTranslator(new IntegerTranslator());
		registerTranslator(new DoubleTranslator());
		registerTranslator(new ListTranslator());
		registerTranslator(new MapTranslator());
		registerTranslator(new StringTranslator());
		registerTranslator(new EnumTranslator());
	}

	public static void registerTranslator(SimpleTranslator<?, ?> converter) {
		for (Class<?> type : converter.types()) {
			TRANSLATOR_LIST.put(type, converter);
		}
	}

	public static <V, R extends Annotation> SimpleTranslator<V, R> getTranslator(SimpleField<?> field) {
		return getTranslator(field.getType());
	}

	@SuppressWarnings("unchecked") // TODO find a better way then using unchecked
	public static <V, R extends Annotation> SimpleTranslator<V, R> getTranslator(Class<?> fieldClass) {
		SimpleTranslator<?, ?> converter = TRANSLATOR_LIST.get(fieldClass);
		if (converter == null) {
			for (Entry<Class<?>, SimpleTranslator<?, ?>> entry : TRANSLATOR_LIST.entrySet()) {
				if (entry.getKey().isAssignableFrom(fieldClass)) {
					return (SimpleTranslator<V, R>) entry.getValue();
				}
			}
		} else {
			return (SimpleTranslator<V, R>) converter;
		}

		return (SimpleTranslator<V, R>) DEFAULT_TRANSLATOR;
	}
}