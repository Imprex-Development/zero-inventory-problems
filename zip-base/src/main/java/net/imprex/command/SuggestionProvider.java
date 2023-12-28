package net.imprex.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

public class SuggestionProvider {

	public static boolean match(String input, String message) {
		int length = Math.min(input.length(), message.length());
		for (int i = 0; i < length; i++) {
			if (message.charAt(i) != input.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Iterator<String> values) {
		String input = builder.getRemaining().toLowerCase();
		while (values.hasNext()) {
			String value = values.next().toLowerCase();
			if (match(input, value)) {
				builder.suggest(value);
			}
		}
		return builder.buildFuture();
	}

	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Collection<String> values) {
		return suggest(builder, values.stream());
	}

	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, String[] values) {
		return suggest(builder, Arrays.stream(values));
	}

	public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Stream<String> values) {
		String input = builder.getRemaining().toLowerCase();
		values.filter(value -> match(input, value.toLowerCase())).forEach(builder::suggest);
		return builder.buildFuture();
	}

	public static final CompletableFuture<Suggestions> compareSuggest(SuggestionsBuilder builder, String input, String prefix, Stream<String> values) {
		return compareSuggest(builder, input, prefix, values.toArray(String[]::new));
	}

	public static final CompletableFuture<Suggestions> compareSuggest(SuggestionsBuilder builder, String input, String prefix, String... values) {
		input = input.toLowerCase();

		for (String value : values) {
			String name = value.toLowerCase();
			if (name.startsWith(input) || name.contains(input)) {
				builder.suggest(prefix + value.toLowerCase());
			}
		}
		return builder.buildFuture();
	}
}