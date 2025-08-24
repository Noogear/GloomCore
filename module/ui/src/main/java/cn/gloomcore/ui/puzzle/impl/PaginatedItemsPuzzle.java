package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.Icon;
import cn.gloomcore.ui.puzzle.DynamicPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 分页物品拼图类，用于在GUI中显示可分页的物品列表
 * <p>
 * 该拼图将一组物品分页显示在指定的槽位中，支持翻页功能，
 * 并能处理物品的点击事件。每页显示的物品数量取决于提供的槽位数量
 */
public class PaginatedItemsPuzzle extends DynamicPuzzle {
    private final List<Integer> slots;
    private final List<Icon> allItems;
    private int currentPage = 0;

    /**
     * 构造一个新的分页物品拼图实例
     *
     * @param slots    用于显示物品的槽位列表
     * @param allItems 所有需要分页显示的物品图标列表
     */
    public PaginatedItemsPuzzle(List<Integer> slots, List<Icon> allItems) {
        this.slots = slots;
        this.allItems = allItems;
    }

    /**
     * 获取拼图占据的所有槽位
     *
     * @return 包含所有槽位索引的列表
     */
    @Override
    public List<Integer> getSlots() {
        return slots;
    }

    /**
     * 渲染当前页的物品到指定库存中
     * <p>
     * 首先清空所有槽位，然后计算当前页应显示的物品范围，
     * 将对应物品设置到槽位中
     *
     * @param player    目标玩家
     * @param inventory 目标库存
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        // 清空所有槽位
        slots.forEach(slot -> inventory.setItem(slot, null));

        int itemsPerPage = slots.size();
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < allItems.size()) {
                inventory.setItem(slots.get(i), allItems.get(itemIndex).display());
            } else {
                break; // 没有更多物品了
            }
        }
    }

    /**
     * 切换到下一页
     * <p>
     * 如果存在下一页，则增加当前页码并更新显示
     *
     * @param player 目标玩家
     * @return 如果成功切换到下一页返回true，否则返回false
     */
    public boolean nextPage(Player player) {
        if ((currentPage + 1) * slots.size() < allItems.size()) {
            currentPage++;
            update(player);
            return true;
        }
        return false;
    }

    /**
     * 切换到上一页
     * <p>
     * 如果存在上一页，则减少当前页码并更新显示
     *
     * @param player 目标玩家
     * @return 如果成功切换到上一页返回true，否则返回false
     */
    public boolean previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            update(player);
            return true;
        }
        return false;
    }

    /**
     * 处理物品点击事件
     * <p>
     * 根据点击的槽位确定对应的物品，然后将事件转发给该物品处理
     *
     * @param event 库存点击事件
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        int clickedRawSlot = event.getRawSlot();
        int slotIndexInPage = this.slots.indexOf(clickedRawSlot);
        if (slotIndexInPage == -1) {
            return;
        }
        int itemsPerPage = this.slots.size();
        int globalItemIndex = (currentPage * itemsPerPage) + slotIndexInPage;
        if (globalItemIndex < allItems.size()) {
            Icon clickedItem = allItems.get(globalItemIndex);
            clickedItem.onClick(event);
        }

    }

}
