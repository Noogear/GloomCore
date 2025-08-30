package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.icon.IconDisplay;
import gloomcore.paper.gui.puzzle.abstracts.StaticPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 复合静态物品拼图实现类
 * <p>
 * 该类用于在一个拼图对象中管理多个不同的静态图标，并将它们精确地渲染到各自指定的槽位上。
 * 它通过遍历父类 AbstractPuzzle 提供的、经过排序和去重的 slots 数组来保证渲染的稳定性和一致性。
 */
public class CombinedItemPuzzleImpl extends StaticPuzzle {
    private final Map<Integer, IconDisplay> slotIconMap;

    /**
     * 构造函数，创建一个复合静态物品拼图实例。
     *
     * @param slotIconMap 一个映射，其中 Key 是槽位索引 (Integer)，Value 是要显示在该槽位的图标 (IconDisplay)。
     *                    拼图将自动占据所有 Map 中定义的槽位。
     */
    public CombinedItemPuzzleImpl(@NotNull Map<Integer, IconDisplay> slotIconMap) {
        super(slotIconMap.keySet());
        this.slotIconMap = slotIconMap;
    }

    /**
     * 拷贝构造函数
     *
     * @param other 需要拷贝的 CombinedItemPuzzleImpl 实例
     */
    public CombinedItemPuzzleImpl(@NotNull CombinedItemPuzzleImpl other) {
        super(other);
        this.slotIconMap = new HashMap<>(other.slotIconMap);
    }

    /**
     * 渲染拼图内容到指定库存中。(已修改)
     * <p>
     * 遍历由父类 AbstractPuzzle 管理的、已排序的 'slots' 数组。
     * 对于数组中的每一个槽位，从 Map 中查找对应的图标并进行渲染。
     * 这种方式完全适配了父类的设计。
     *
     * @param player    目标玩家
     * @param inventory 目标库存
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        for (int slot : this.slots) {
            IconDisplay display = this.slotIconMap.get(slot);
            if (display != null) {
                ItemStack itemStack = display.parse(player);
                inventory.setItem(slot, itemStack);
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
