package net.imprex.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SuggestionBuilder<TRoot, TOut, TSender> {

	private final List<BiFunction<Stream, CommandContext<TSender>, Stream>> transformations = new ArrayList<>();
	private final Supplier<Stream<TRoot>> supplier;

	public SuggestionBuilder(Supplier<Stream<TRoot>> supplier) {
		this.supplier = supplier;
	}

	public SuggestionBuilder<TRoot, TOut, TSender> filter(Predicate<TOut> predicate) {
		this.transformations.add((stream, context) -> stream.filter(predicate));
		return this;
	}

	public SuggestionBuilder<TRoot, TOut, TSender> filter(BiPredicate<TOut, CommandContext<TSender>> predicate) {
		this.transformations.add((stream, context) -> stream.filter(value -> predicate.test((TOut) value, context)));
		return this;
	}

	public <TMap> SuggestionBuilder<TRoot, TMap, TSender> map(Function<TOut, TMap> function) {
		this.transformations.add((stream, context) -> stream.map(function));
		return (SuggestionBuilder<TRoot, TMap, TSender>) this;
	}

	public <TMap> SuggestionBuilder<TRoot, TMap, TSender> flatMap(Function<TOut, TMap> function) {
		this.transformations.add((stream, context) -> stream.flatMap(function));
		return (SuggestionBuilder<TRoot, TMap, TSender>) this;
	}

	public Function<Stream<TRoot>, Stream<TOut>> buildStream(CommandContext<TSender> context) {
		return stream -> {
			for (BiFunction<Stream, CommandContext<TSender>, Stream> transformation : this.transformations) {
				stream = transformation.apply(stream, context);
			}
			return (Stream<TOut>) stream;
		};
	}

	public Function<Iterable<TRoot>, Stream<TOut>> buildIterable(CommandContext<TSender> context) {
		return (iterable) -> {
			Stream stream = StreamSupport.stream(iterable.spliterator(), false);
			for (var transformation : transformations) {
				stream = transformation.apply(stream, context);
			}
			return (Stream<TOut>) stream;
		};
	}

	public SuggestionProvider<TSender> buildSuggest() {
		return (context, builder) -> {
			String input = builder.getRemaining().toLowerCase();
			this.buildStream(context)
					.apply(this.supplier.get())
					.map(Objects::toString)
					.filter(Objects::nonNull)
					.filter(name -> {
						if (input == null || name.toLowerCase().startsWith(input)) {
							return true;
						}
						return false;
					})
					.forEach(builder::suggest);
			return builder.buildFuture();
		};
	}
}