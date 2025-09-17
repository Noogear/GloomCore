package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.icon.Icon;
import gloomcore.paper.gui.puzzle.PaginatedPuzzle;
import gloomcore.paper.gui.puzzle.abstracts.DynamicPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页物品拼图类，用于在GUI中显示可分页的物品列表
 * <p>
 * 该拼图将一组物品分页显示在指定的槽位中，支持翻页功能，
 * 并能处理物品的点击事件。每页显示的物品数量取决于提供的槽位数量
 */
public class PaginatedPuzzleImpl<C extends Context> extends DynamicPuzzle<C> implements PaginatedPuzzle<C> {
    private final List<Icon> allItems;
    private final ItemStack backgroundItem;
    private int currentPage = 0;

    /**
     * 构造一个新的分页物品拼图实例
     *
     * @param slotList 用于显示物品的槽位列表
     * @param allItems 所有需要分页显示的物品图标列表
     */
    public PaginatedPuzzleImpl(Collection<Integer> slotList, List<Icon> allItems) {
        this(slotList, allItems, null);
    }

    /**
     * 构造一个新的分页物品拼图实例
     *
     * @param slotList       用于显示物品的槽位列表
     * @param allItems       所有需要分页显示的物品图标列表
     * @param backgroundItem 背景物品，用于填充没有内容的槽位
     */
    public PaginatedPuzzleImpl(Collection<Integer> slotList, List<Icon> allItems, @Nullable ItemStack backgroundItem) {
        super(slotList);
        this.allItems = allItems;
        this.backgroundItem = backgroundItem;
    }

    /**
     * 拷贝构造函数，基于另一个PaginatedPuzzleImpl实例创建新实例
     * <p>
     * 该构造函数会深拷贝图标列表，为每个图标创建新的实例，
     * 并复制当前页码
     *
     * @param other 需要拷贝的PaginatedPuzzleImpl实例
     */
    public PaginatedPuzzleImpl(@NotNull PaginatedPuzzleImpl<C> other) {
        super(other);
        this.allItems = other.allItems.stream()
                .map(Icon::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.currentPage = other.currentPage;
        this.backgroundItem = other.backgroundItem;
    }


    /**
     * 渲染当前页的物品到指定库存中
     * <p>
     * 首先清空所有槽位或设置背景物品，然后计算当前页应显示的物品范围，
     * 将对应物品设置到槽位中
     *
     * @param context  目标上下文
     * @param inventory 目标库存
     */
    @Override
    public void render(C context, @NotNull Inventory inventory) {
        // 先为所有槽位设置背景物品或清空
        for (int slot : slots) {
            inventory.setItem(slot, backgroundItem);
        }

        int itemsPerPage = slots.length;
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < allItems.size()) {
                inventory.setItem(slots[i], allItems.get(itemIndex).display(context.player()));
            } else {
                break;
            }
        }
    }

    /**
     * 切换到下一页
     * <p>
     * 如果存在下一页，则增加当前页码并更新显示
     *
     * @param context 目标上下文
     * @return 如果成功切换到下一页返回true，否则返回false
     */
    @Override
    public boolean nextPage(C context) {
        if ((currentPage + 1) * slots.length < allItems.size()) {
            currentPage++;
            update(context);
            return true;
        }
        return false;
    }

    /**
     * 切换到上一页
     * <p>
     * 如果存在上一页，则减少当前页码并更新显示
     *
     * @param context 目标上下文
     * @return 如果成功切换到上一页返回true，否则返回false
     */
    @Override
    public boolean previousPage(C context) {
        if (currentPage > 0) {
            currentPage--;
            update(context);
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
    public void onClick(InventoryClickEvent event, C owner) {
        int clickedRawSlot = event.getRawSlot();
        int slotIndexInPage = Arrays.binarySearch(slots, clickedRawSlot);
        if (slotIndexInPage >= 0) {
            int itemsPerPage = this.slots.length;
            int globalItemIndex = (currentPage * itemsPerPage) + slotIndexInPage;
            if (globalItemIndex < allItems.size()) {
                Icon clickedItem = allItems.get(globalItemIndex);
                Player player = owner.player();
                clickedItem.onClick(event.getClick(), player);
            }
        }
    }

    /**
     * 直接跳转到指定页码。
     *
     * @param pageNumber 用户输入的页码 (1-based, 即第一页是1)。
     * @param context     目标上下文。
     * @return 如果页面成功跳转则返回true，如果目标页码与当前页码相同则返回false。
     */
    @Override
    public boolean jumpToPage(int pageNumber, C context) {
        int totalPages = getTotalPages();
        int targetPageIndex = pageNumber - 1;

        if (targetPageIndex < 0) {
            targetPageIndex = 0;
        } else if (targetPageIndex >= totalPages) {
            targetPageIndex = totalPages - 1;
        }
        if (this.currentPage == targetPageIndex) {
            return false;
        }

        this.currentPage = targetPageIndex;
        update(context);
        return true;
    }

    /**
     * 获取当前页码 (1-based, 用于显示)。
     *
     * @return 当前页码，从1开始。
     */
    @Override
    public int getCurrentPage() {
        return this.currentPage + 1;
    }

    /**
     * 获取总页数。
     *
     * @return 总页数，至少为1。
     */
    @Override
    public int getTotalPages() {
        if (allItems.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) allItems.size() / slots.length);
    }

}
