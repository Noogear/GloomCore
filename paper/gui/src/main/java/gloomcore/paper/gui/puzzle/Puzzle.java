package gloomcore.paper.gui.puzzle;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * 拼图接口，定义了GUI中可交互组件的基本行为
 * <p>
 * 拼图是GUI界面的基本组成单元，每个拼图可以占据一个或多个槽位，
 * 并能处理点击事件、渲染内容和更新显示
 */
public interface Puzzle {

    /**
     * 获取拼图占据的所有槽位
     *
     * @return 包含所有槽位索引的集合
     */
    int[] getSlots();

    /**
     * 渲染拼图内容到指定库存中
     *
     * @param player    目标玩家
     * @param inventory 目标库存
     */
    void render(Player player, @NotNull Inventory inventory);

    /**
     * 处理库存点击事件
     *
     * @param event 库存点击事件
     */
    void onClick(InventoryClickEvent event);

    /**
     * 更新拼图显示内容
     *
     * @param player 目标玩家
     */
    void update(Player player);

    /**
     * 获取拼图类型
     *
     * @return 拼图类型枚举值
     */
    PuzzleType getPuzzleType();


    /**
     * 拼图类型枚举，定义了不同类型的拼图
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
