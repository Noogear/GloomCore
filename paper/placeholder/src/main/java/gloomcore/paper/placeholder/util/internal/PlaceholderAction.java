package gloomcore.paper.placeholder.util.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 代表占位符的“动作”或“文本来源”。
 * 它本身就是一个 Placeholder，用于定义如何生成最终的文本。
 */
@FunctionalInterface
public interface PlaceholderAction extends Placeholder {

    /**
     * 从一个固定的字符串创建一个动作。
     */
    @NotNull
    static PlaceholderAction of(@NotNull String staticText) {
        return player -> staticText;
    }

    /**
     * 从一个不依赖玩家的提供者创建一个动作 (例如：服务器TPS, 在线人数)。
     */
    @NotNull
    static PlaceholderAction of(@NotNull Supplier<String> supplier) {
        return player -> supplier.get();
    }

    /**
     * 从一个依赖玩家的函数创建一个动作 (例如：玩家生命值, 所在世界)。
     */
    @NotNull
    static PlaceholderAction of(@NotNull Function<Player, String> function) {
        return function::apply;
    }
}
