package gloomcore.paper.placeholder.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@FunctionalInterface
public interface Placeholder {

    @Nullable
    String apply(@Nullable Player player, @NotNull String[] args);

    @Nullable
    default String apply(@NotNull String[] args) {
        return apply(null, args);
    }

}