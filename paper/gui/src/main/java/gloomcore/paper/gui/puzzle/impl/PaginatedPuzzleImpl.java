package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.icon.Icon;
import gloomcore.paper.gui.puzzle.PaginatedPuzzle;
import gloomcore.paper.gui.puzzle.abstracts.DynamicPuzzle;
import gloomcore.paper.gui.view.AbstractGui;
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
 * 分页拼图：在给定槽位上分页展示一组图标，并处理翻页与点击事件。
 * 每页可展示的数量 = 槽位数量。
 */
public class PaginatedPuzzleImpl<C extends Context> extends DynamicPuzzle<C> implements PaginatedPuzzle<C> {
    private final List<Icon<C>> allItems;
    private final ItemStack backgroundItem;
    private int currentPage = 0;

    /**
     * 绑定 GUI 的构造函数。
     */
    public PaginatedPuzzleImpl(Collection<Integer> slotList, List<Icon<C>> allItems, @Nullable ItemStack backgroundItem, AbstractGui<C> gui) {
        super(slotList, gui);
        this.allItems = allItems;
        this.backgroundItem = backgroundItem;
    }

    /**
     * 拷贝构造（保留原 GUI 绑定），重置页码为 0。
     */
    public PaginatedPuzzleImpl(@NotNull PaginatedPuzzleImpl<C> other) {
        super(other);
        this.allItems = other.allItems.stream()
                .map(Icon::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.currentPage = 0;
        this.backgroundItem = other.backgroundItem;
    }

    /**
     * 拷贝构造（绑定到新 GUI），重置页码为 0。
     */
    public PaginatedPuzzleImpl(@NotNull PaginatedPuzzleImpl<C> other, @NotNull AbstractGui<C> gui) {
        super(other, gui);
        this.allItems = other.allItems.stream()
                .map(Icon::new)
                .collect(Collectors.toCollection(ArrayList::new));
        this.currentPage = 0;
        this.backgroundItem = other.backgroundItem;
    }

    /**
     * 渲染当前页的物品到指定库存。
     * 先为所有槽位设置背景物品（或清空），再填入当前页的图标。
     */
    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, backgroundItem);
        }
        int itemsPerPage = slots.length;
        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < allItems.size()) {
                inventory.setItem(slots[i], allItems.get(itemIndex).display(context));
            } else {
                break;
            }
        }
    }

    /**
     * 下一页。
     */
    @Override
    public boolean nextPage() {
        if ((currentPage + 1) * slots.length < allItems.size()) {
            currentPage++;
            update();
            return true;
        }
        return false;
    }

    /**
     * 上一页。
     */
    @Override
    public boolean previousPage() {
        if (currentPage > 0) {
            currentPage--;
            update();
            return true;
        }
        return false;
    }

    /**
     * 将点击事件转发给当前页对应的图标处理。
     */
    @Override
    public void onClick(InventoryClickEvent event, C owner) {
        int clickedRawSlot = event.getRawSlot();
        int slotIndexInPage = Arrays.binarySearch(slots, clickedRawSlot);
        if (slotIndexInPage >= 0) {
            int itemsPerPage = this.slots.length;
            int globalItemIndex = (currentPage * itemsPerPage) + slotIndexInPage;
            if (globalItemIndex < allItems.size()) {
                Icon<C> clickedItem = allItems.get(globalItemIndex);
                clickedItem.onClick(event, owner);
            }
        }
    }

    /**
     * 跳转到指定页（1-based）。越界会被裁剪到合法范围。
     */
    @Override
    public boolean jumpToPage(int pageNumber) {
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
        update();
        return true;
    }

    /** 当前页（1-based）。 */
    @Override
    public int getCurrentPage() {
        return this.currentPage + 1;
    }

    /** 总页数（至少 1）。 */
    @Override
    public int getTotalPages() {
        if (allItems.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) allItems.size() / slots.length);
    }
}
