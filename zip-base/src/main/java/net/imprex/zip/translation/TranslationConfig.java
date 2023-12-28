package net.imprex.zip.translation;

import java.util.HashMap;
import java.util.Map;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSectionRoot;

@SimpleSectionRoot(name = "translation", version = 1)
public class TranslationConfig {

	private static final Map<String, String> DEFAULT_REPLACEMENT = new HashMap<>();
	private static final Map<String, String> DEFAULT_MESSAGE = new HashMap<>();

	static {
		DEFAULT_REPLACEMENT.put("PREFIX", Message.PREFIX.getDefaultMessage());
		DEFAULT_REPLACEMENT.put("§g", "#808080#");

		for (Message message : Message.values()) {
			if (message == Message.PREFIX) {
				DEFAULT_MESSAGE.put(message.getKey(), "PREFIX");
			} else {
				DEFAULT_MESSAGE.put(message.getKey(), "PREFIX " + message.getDefaultMessage());
			}
		}
	}

	@SimpleKey
	private Map<String, String> replacement = DEFAULT_REPLACEMENT;

	@SimpleKey
	private Map<String, String> message = DEFAULT_MESSAGE;

	public Map<String, String> getReplacement() {
		return this.replacement;
	}

	public Map<String, String> getMessage() {
		return this.message;
	}
}
