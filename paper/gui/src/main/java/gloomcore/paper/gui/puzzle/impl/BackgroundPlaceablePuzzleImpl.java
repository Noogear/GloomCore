package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.abstracts.AbstractPuzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class BackgroundPlaceablePuzzleImpl<C extends Context> extends AbstractPuzzle<C> implements PlaceablePuzzle<C> {
    private final Consumer<C> onContentsChanged;
    private final boolean stackingEnabled;
    private final ItemStack backgroundItem;

    /**
     * 构造函数，创建一个带背景板的可放置物品的拼图实例
     *
     * @param slotList          拼图占据的槽位列表
     * @param backgroundItem    作为背景板的物品实例
     * @param onContentsChanged 内容变更时的回调函数，可为null
     * @param stackingEnabled   是否启用物品堆叠功能
     */
    public BackgroundPlaceablePuzzleImpl(@NotNull Collection<Integer> slotList, @NotNull ItemStack backgroundItem, @Nullable Consumer<C> onContentsChanged, boolean stackingEnabled) {
        super(slotList);
        this.backgroundItem = backgroundItem;
        this.onContentsChanged = onContentsChanged;
        this.stackingEnabled = stackingEnabled;
    }

    /**
     * 拷贝构造函数
     *
     * @param other 需要拷贝的 BackgroundPlaceablePuzzleImpl 实例
     */
    public BackgroundPlaceablePuzzleImpl(@NotNull BackgroundPlaceablePuzzleImpl<C> other) {
        super(other);
        this.onContentsChanged = other.onContentsChanged;
        this.stackingEnabled = other.stackingEnabled;
        this.backgroundItem = other.backgroundItem.clone();
    }

    /**
     * 渲染拼图内容到指定库存中
     * <p>
     * 将所有指定槽位设置为背景物品
     *
     * @param context  目标上下文
     * @param inventory 目标库存
     */
    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, backgroundItem.clone());
        }
    }

    /**
     * 处理槽位点击事件
     * <p>
     * 核心逻辑：
     * 1. 阻止玩家拾取背景物品。
     * 2. 允许玩家用自己的物品替换背景物品。
     * 3. 当玩家取走自己的物品后，自动恢复背景物品。
     * 4. 在内容变化后触发回调。
     *
     * @param event 库存点击事件
     */
    @Override
    public void onClick(InventoryClickEvent event, C owner) {
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (cursorItem != null && !cursorItem.isEmpty()) {
            event.setCancelled(false);
        } else if (currentItem != null && currentItem.isSimilar(backgroundItem)) {
            event.setCancelled(true);
            return;
        } else {
            event.setCancelled(false);
        }
        PaperScheduler.INSTANCE.entity(event.getWhoClicked()).runDelayed(() -> {
            ItemStack itemAfterClick = event.getInventory().getItem(event.getSlot());
            if (itemAfterClick == null || itemAfterClick.getType().isAir()) {
                event.getInventory().setItem(event.getSlot(), backgroundItem.clone());
            }
            if (onContentsChanged != null) {
                onContentsChanged.accept(owner);
            }
        }, 1L);
    }

    /**
     * 更新拼图显示内容
     * <p>
     * 此拼图为静态类型，不需要更新操作
     *
     * @param context 目标上下文
     */
    @Override
    public void update(C context) {
    }

    /**
     * GUI关闭时执行清理操作，将玩家放置的物品归还
     * <p>
     * 核心逻辑：只归还非背景板的物品。
     *
     * @param context   关闭GUI的上下文
     * @param inventory 被关闭的GUI的Inventory实例
     */
    @Override
    public void cleanupOnClose(C context, Inventory inventory) {
        List<ItemStack> itemsToReturn = new ArrayList<>();
        for (int slot : this.getSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.isEmpty() && !item.isSimilar(backgroundItem)) {
                itemsToReturn.add(item);
                inventory.setItem(slot, null);
            }
        }
        returnItemsToPlayer(context, itemsToReturn);
    }

    /**
     * 尝试接受物品到拼图中 (例如：Shift + 点击)
     * <p>
     * 核心逻辑：将持有背景板的槽位视为空槽位来接受物品。
     *
     * @param itemToAccept 要接受的物���
     * @param inventory    GUI库存实例
     * @return 如果成功接受至少一部分物品，返回true
     */
    @Override
    public boolean tryAcceptItem(ItemStack itemToAccept, Inventory inventory) {
        if (itemToAccept == null || itemToAccept.getAmount() <= 0) {
            return false;
        }
        int initialAmount = itemToAccept.getAmount();

        if (this.stackingEnabled) {
            for (int slot : this.slots) {
                ItemStack existingItem = inventory.getItem(slot);
                if (existingItem != null && !existingItem.isSimilar(backgroundItem) && existingItem.isSimilar(itemToAccept)) {
                    int space = existingItem.getMaxStackSize() - existingItem.getAmount();
                    if (space > 0) {
                        int amountToMove = Math.min(space, itemToAccept.getAmount());
                        existingItem.add(amountToMove);
                        itemToAccept.subtract(amountToMove);
                    }
                }
                if (itemToAccept.getAmount() <= 0) {
                    return true;
                }
            }
        }

        if (itemToAccept.getAmount() > 0) {
            for (int slot : this.slots) {
                ItemStack slotItem = inventory.getItem(slot);
                if (slotItem == null || slotItem.isEmpty() || slotItem.isSimilar(backgroundItem)) {
                    inventory.setItem(slot, itemToAccept.clone());
                    itemToAccept.setAmount(0);
                    return true;
                }
            }
        }

        return itemToAccept.getAmount() < initialAmount;
    }

    @Override
    public Consumer<C> getChangedCallBack() {
        return this.onContentsChanged;
    }

    @Override
    public boolean hasChangedCallBack() {
        return this.onContentsChanged != null;
    }

}
