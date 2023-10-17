package net.imprex.zip.config.v3;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.imprex.zip.config.v3.translator.BooleanTranslator;
import net.imprex.zip.config.v3.translator.IntegerTranslator;
import net.imprex.zip.config.v3.translator.ListTranslator;
import net.imprex.zip.config.v3.translator.ObjectTranslator;
import net.imprex.zip.config.v3.translator.StringTranslator;

public class SimpleTranslatorRegistry {

	private static final Map<Class<?>, SimpleTranslator<?, ?>> TRANSLATOR_LIST = new HashMap<>();
	private static final ObjectTranslator DEFAULT_TRANSLATOR = new ObjectTranslator();

	static {
		registerTranslator(new BooleanTranslator());
		registerTranslator(new IntegerTranslator());
		registerTranslator(new StringTranslator());
		registerTranslator(new ListTranslator());
	}

	public static void registerTranslator(SimpleTranslator<?, ?> converter) {
		for (Class<?> type : converter.types()) {
			TRANSLATOR_LIST.put(type, converter);
		}
	}

	@SuppressWarnings("unchecked") // TODO find a better way then using unchecked
	public static <V, R extends Annotation> SimpleTranslator<V, R> getTranslator(Class<V> classType) {
		SimpleTranslator<?, ?> converter = TRANSLATOR_LIST.get(classType);
		if (converter == null) {
			for (Entry<Class<?>, SimpleTranslator<?, ?>> entry : TRANSLATOR_LIST.entrySet()) {
				if (entry.getKey().isAssignableFrom(classType)) {
					return (SimpleTranslator<V, R>) entry.getValue();
				}
			}
		} else {
			return (SimpleTranslator<V, R>) converter;
		}

		return (SimpleTranslator<V, R>) DEFAULT_TRANSLATOR;
	}
}