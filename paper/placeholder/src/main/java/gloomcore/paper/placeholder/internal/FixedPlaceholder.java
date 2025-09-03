package gloomcore.paper.placeholder.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface FixedPlaceholder extends Placeholder {

    /**
     * 从一个固定的字符串创建一个动作。
     */
    @NotNull
    static FixedPlaceholder of(@NotNull String staticText) {
        return player -> staticText;
    }

    /**
     * 从一个不依赖玩家的提供者创建一个动作 (例如：服务器TPS, 在线人数)。
     */
    @NotNull
    static FixedPlaceholder of(@NotNull Supplier<String> supplier) {
        return player -> supplier.get();
    }

    /**
     * 从一个依赖玩家的函数创建一个动作 (例如：玩家生命值, 所在世界)。
     */
    @NotNull
    static FixedPlaceholder of(@NotNull Function<Player, String> function) {
        return function::apply;
    }

    @Override
    default @Nullable String apply(@Nullable Player player, @NotNull String[] args) {
        return apply(player);
    }

    String apply(@Nullable Player player);

    default String apply() {
        return apply((Player) null);
    }
}
