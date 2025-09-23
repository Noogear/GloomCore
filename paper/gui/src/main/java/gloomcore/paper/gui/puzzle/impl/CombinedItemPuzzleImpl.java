package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.contract.Context;
import gloomcore.paper.gui.puzzle.abstracts.StaticPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 复合静态物品拼图实现类
 * <p>
 * 该类用于在一个拼图对象中管理多个不同的静态图标，并将它们精确地渲染到各自指定的槽位上。
 * 它通过遍历父类 AbstractPuzzle 提供的、经过排序和去重的 slots 数组来保证渲染的稳定性和一致性。
 */
public class CombinedItemPuzzleImpl<C extends Context<Player>> extends StaticPuzzle<C> {
    private final Map<Integer, Function<C, ItemStack>> slotIconMap;

    /**
     * 构造函数，创建一个复合静态物品拼图实例。
     *
     * @param slotIconMap 一个映射，其中 Key 为槽位索引 (Integer)，Value 为在该槽位显示的图标生成函数。
     *                    拼图将自动占据所有 Map 中定义的槽位。
     */
    public CombinedItemPuzzleImpl(@NotNull Map<Integer, Function<C, ItemStack>> slotIconMap) {
        super(slotIconMap.keySet());
        this.slotIconMap = slotIconMap;
    }

    /**
     * 拷贝构造函数
     *
     * @param other 需要拷贝的 CombinedItemPuzzleImpl 实例
     */
    public CombinedItemPuzzleImpl(@NotNull CombinedItemPuzzleImpl<C> other) {
        super(other);
        this.slotIconMap = new HashMap<>(other.slotIconMap);
    }

    /**
     * 渲染拼图内容到指定库存中。
     * <p>
     * 遍历父类 AbstractPuzzle 管理的已排序 slots 数组，
     * 对每个槽位从 Map 中取出对应图标并进行渲染。
     */
    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : this.slots) {
            Function<C, ItemStack> display = this.slotIconMap.get(slot);
            if (display != null) {
                inventory.setItem(slot, display.apply(context));
            } else {
                inventory.setItem(slot, null);
            }
        }
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }
}
