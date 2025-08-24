package cn.gloomcore.ui.icon;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 动作接口，表示可以执行的操作
 * <p>
 * 该接口是一个函数式接口，允许通过lambda表达式或方法引用来实现
 * 提供了两种执行方式：带玩家参数和不带参数的执行方式
 */
@FunctionalInterface
public interface Action {

    /**
     * 执行动作
     *
     * @param player 执行动作的玩家，可能为null
     */
    void run(@Nullable Player player);

    /**
     * 执行动作，不指定玩家
     * <p>
     * 此方法会调用 {@link #run(Player)} 并传入null作为参数
     */
    default void run() {
        run(null);
    }
}
