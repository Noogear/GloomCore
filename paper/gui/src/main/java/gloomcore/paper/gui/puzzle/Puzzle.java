package gloomcore.paper.gui.puzzle;

import gloomcore.paper.gui.context.Context;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * 拼图接口：定义 GUI 中可交互组件的最小行为集合。
 * <p>
 * 每个拼图可以占据一个或多个槽位，能够将自身渲染到指定的 Inventory，
 * 并在对应槽位被点击时处理交互事件。
 */
public interface Puzzle<C extends Context> {

    /**
     * 获取拼图占用的所有槽位。
     *
     * @return 已排序且去重后的槽位索引数组
     */
    int[] getSlots();

    /**
     * 将拼图内容渲染到指定库存。
     *
     * @param context   业务上下文（由玩家 UUID 构造）
     * @param inventory 目标库存
     */
    void render(C context, @NotNull Inventory inventory);

    /**
     * 处理库存点击事件。
     * <p>
     * 当玩家点击与该拼图相关的槽位时调用。实现类应根据需求执行相应逻辑。
     *
     * @param event   库存点击事件
     * @param context 拼图所属的上下文
     */
    void onClick(InventoryClickEvent event, C context);

    /**
     * 获取拼图类型。
     *
     * @return 拼图类型枚举值
     */
    PuzzleType getPuzzleType();

    /**
     * 拼图类型枚举。
     * <p>
     * COMMON: 普通拼图
     * PAGINATED: 分页拼图
     * PLACEABLE: 可放置物品的拼图
     */
    enum PuzzleType {
        COMMON,
        PAGINATED,
        PLACEABLE
    }
}
