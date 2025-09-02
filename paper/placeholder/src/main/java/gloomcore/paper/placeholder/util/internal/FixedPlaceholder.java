package gloomcore.paper.placeholder.util.internal;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 代表一个可处理的占位符的核心接口。
 * 这是一个函数式接口，其核心方法是 {@link #apply(Player)}。
 */
@FunctionalInterface
public interface FixedPlaceholder {

    /**
     * 根据给定的玩家处理占位符并返回结果字符串。
     *
     * @param player 可能是 null 的玩家对象。
     * @return 处理后的字符串，如果无法处理则为 null。
     */
    @Nullable
    String apply(@Nullable Player player);

    /**
     * 在没有玩家上下文的情况下处理占位符。
     * 这是 {@code apply(null)} 的一个便捷方法。
     *
     * @return 处理后的字符串。
     */
    @Nullable
    default String apply() {
        return apply(null);
    }
}