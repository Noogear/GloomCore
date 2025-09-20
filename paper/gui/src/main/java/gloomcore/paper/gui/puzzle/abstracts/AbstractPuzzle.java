package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.contract.Context;
import gloomcore.paper.gui.puzzle.Puzzle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 抽象拼图类，实现了Puzzle接口
 * <p>
 * 该类提供了拼图的基本实现，包括槽位管理等通用功能。
 * </p>
 */
public abstract class AbstractPuzzle<C extends Context<Player>> implements Puzzle<C> {
    protected final int[] slots;

    /**
     * 构造函数，根据槽位集合创建AbstractPuzzle实例
     *
     * @param slotList 槽位集合，不能为空
     * @throws IllegalArgumentException 当slotList为空时抛出
     */
    protected AbstractPuzzle(@NotNull Collection<Integer> slotList) {
        if (slotList.isEmpty()) {
            throw new IllegalArgumentException("slotList cannot be empty");
        }
        this.slots = slotList.stream()
                .distinct()
                .sorted()
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 AbstractPuzzle 对象
     */
    protected AbstractPuzzle(AbstractPuzzle<C> other) {
        this.slots = other.slots.clone();
    }

    /**
     * 获取拼图占用的槽位数组
     *
     * @return 包含所有槽位索引的有序数组
     */
    @Override
    public int[] getSlots() {
        return slots;
    }

}
