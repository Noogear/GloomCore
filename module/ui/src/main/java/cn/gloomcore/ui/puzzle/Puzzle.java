package cn.gloomcore.ui.puzzle;

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


}
