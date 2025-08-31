package gloomcore.contract.replacer;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

@FunctionalInterface
public interface StringReplacer {

    static StringReplacer combine(@NotNull Collection<? extends StringReplacer> stringReplacers) {
        return new StringReplacer() {
            @Override
            public @NotNull String apply(@NotNull String original) {
                String replaced = original;
                for (StringReplacer replacer : stringReplacers) {
                    String newReplaced = replacer.apply(replaced);
                    if (newReplaced != null) {
                        replaced = newReplaced;
                    }
                }
                return replaced;
            }

            @Override
            public @NotNull String apply(@NotNull String original, @NotNull UUID uuid) {
                String replaced = original;
                for (StringReplacer replacer : stringReplacers) {
                    String newReplaced = replacer.apply(replaced, uuid);
                    if (newReplaced != null) {
                        replaced = newReplaced;
                    }
                }
                return replaced;
            }
        };
    }

    @NotNull
    static StringReplacer combine(@NotNull StringReplacer... stringReplacers) {
        return combine(Arrays.asList(stringReplacers));
    }

    @NotNull
    static StringReplacer of(@NotNull UnaryOperator<String> operator, @NotNull BiFunction<String, UUID, String> function) {
        return new StringReplacer() {

            @Override
            public String apply(@NotNull String original) {
                return operator.apply(original);
            }

            @Override
            public String apply(@NotNull String original, @NotNull UUID uuid) {
                return function.apply(original, uuid);
            }
        };
    }

    @NotNull
    static StringReplacer of(@NotNull UnaryOperator<String> operator) {
        return operator::apply;
    }

    @NotNull
    static StringReplacer of(@NotNull BiFunction<String, UUID, String> function) {
        return of(s -> s, function);
    }

    @NotNull
    static StringReplacer ofCache(@NotNull UnaryOperator<String> operator, @NotNull BiFunction<String, UUID, String> function) {
        return new StringReplacer() {
            private final WeakHashMap<String, String> cache = new WeakHashMap<>();

            @Override
            public String apply(@NotNull String original) {
                String result = cache.get(original);
                if (result == null) {
                    result = operator.apply(original);
                    if (result == null) {
                        return original;
                    }
                    cache.put(original, result);
                }
                return result;
            }

            @Override
            public @NotNull String apply(@NotNull String original, @NotNull UUID uuid) {
                String result = cache.get(original);
                if (result == null) {
                    result = function.apply(original, uuid);
                    if (result == null) {
                        return original;
                    }
                    cache.put(original, result);
                }
                return result;
            }
        };
    }

    @NotNull
    static StringReplacer ofCache(@NotNull UnaryOperator<String> operator) {
        return ofCache(operator, (s, uuid) -> operator.apply(s));
    }

    @NotNull
    static StringReplacer ofCache(@NotNull BiFunction<String, UUID, String> function) {
        return ofCache(s -> s, function);
    }

    String apply(@NotNull String original);

    default String apply(@NotNull String original, @NotNull UUID uuid) {
        return apply(original);
    }

}
