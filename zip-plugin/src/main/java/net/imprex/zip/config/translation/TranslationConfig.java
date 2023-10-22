package net.imprex.zip.config.translation;

import java.util.Map;
import java.util.stream.Stream;

import net.imprex.zip.common.Entry;

public class TranslationConfig {

	@SuppressWarnings("unchecked")
	Map<Message, String> translation = Map.ofEntries(
			Stream.of(Message.values())
			.map(key -> new Entry<>(key, key.getDefaultMessage()))
			.toArray(Entry[]::new));
}