package gloomcore.paper.placeholder.fixedPlaceholder.decorator;

import gloomcore.paper.placeholder.fixedPlaceholder.FixedPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface ActionDecorator extends FixedPlaceholder {


    @NotNull
    static ActionDecorator of(@NotNull String text) {
        return player -> text;
    }

    @NotNull
    static ActionDecorator of(@NotNull Supplier<String> supplier) {
        return new ActionDecorator() {

            @Override
            public @Nullable String process(@Nullable Player player) {
                return process();
            }

            @Override
            public @Nullable String process() {
                return supplier.get();
            }
        };
    }

    @NotNull
    static ActionDecorator of(@NotNull Function<Player, String> function) {
        return new ActionDecorator() {

            @Override
            public @Nullable String process(@Nullable Player player) {
                return function.apply(player);
            }

            @Override
            public @Nullable String process() {
                return null;
            }
        };
    }


}
