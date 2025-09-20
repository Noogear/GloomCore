package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.contract.Context;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.view.AbstractGui;
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

/**
 * 可放置类拼图的抽象基类：抽取变更回调、清理归还、堆叠与放置等共性逻辑。
 * 子类仅需实现渲染与（如需）自定义点击行为；也可覆写判定背景/可堆叠/是否归还的钩子以改变策略。
 */
public abstract class PlaceableBasePuzzle<C extends Context<Player>> extends AbstractPuzzle<C> implements PlaceablePuzzle<C> {
    protected final @Nullable Consumer<C> onContentsChanged;
    protected final boolean stackingEnabled;
    protected final AbstractGui<C> gui;

    protected PlaceableBasePuzzle(@NotNull Collection<Integer> slotList,
                                  @Nullable Consumer<C> onContentsChanged,
                                  boolean stackingEnabled,
                                  @NotNull AbstractGui<C> gui) {
        super(slotList);
        this.onContentsChanged = onContentsChanged;
        this.stackingEnabled = stackingEnabled;
        this.gui = gui;
    }

    protected PlaceableBasePuzzle(@NotNull PlaceableBasePuzzle<C> other) {
        super(other);
        this.onContentsChanged = other.onContentsChanged;
        this.stackingEnabled = other.stackingEnabled;
        this.gui = other.gui;
    }

    protected PlaceableBasePuzzle(@NotNull PlaceableBasePuzzle<C> other, @NotNull AbstractGui<C> gui) {
        super(other);
        this.onContentsChanged = other.onContentsChanged;
        this.stackingEnabled = other.stackingEnabled;
        this.gui = gui;
    }

    /**
     * 默认点击后允许物品交互，且在下一刻触发 onContentsChanged 回调（若存在）。
     */
    @Override
    public void onClick(InventoryClickEvent event, C owner) {
        event.setCancelled(false);
        if (onContentsChanged != null) {
            // 交给具体视图的实体线程去调度（避免主线程立刻读取未稳定的inventory状态）。
            PaperScheduler.INSTANCE
                    .entity(event.getWhoClicked())
                    .runDelayed(() -> onContentsChanged.accept(gui.getOwner()), 1L);
        }
    }


    /**
     * Shift-点击等场景的自动接收：支持堆叠+空槽放置策略。
     * 子类可通过覆写 canStackWithExisting/isAcceptableTarget 改变行为（例如背景槽视为空）。
     */
    @Override
    public boolean tryAcceptItem(ItemStack itemToAccept, Inventory inventory) {
        if (itemToAccept == null || itemToAccept.getAmount() <= 0) return false;
        final int initial = itemToAccept.getAmount();

        if (stackingEnabled) {
            for (int slot : this.slots) {
                ItemStack existing = inventory.getItem(slot);
                if (canStackWithExisting(existing, itemToAccept)) {
                    int space = existing.getMaxStackSize() - existing.getAmount();
                    if (space > 0) {
                        int move = Math.min(space, itemToAccept.getAmount());
                        existing.add(move);
                        itemToAccept.subtract(move);
                    }
                }
                if (itemToAccept.getAmount() <= 0) return true;
            }
        }

        if (itemToAccept.getAmount() > 0) {
            for (int slot : this.slots) {
                ItemStack existing = inventory.getItem(slot);
                if (isAcceptableTarget(existing)) {
                    inventory.setItem(slot, itemToAccept.clone());
                    itemToAccept.setAmount(0);
                    return true;
                }
            }
        }
        return itemToAccept.getAmount() < initial;
    }

    /**
     * 钩子：是否可与现有物品进行堆叠（默认：相似即可）。
     */
    protected boolean canStackWithExisting(@Nullable ItemStack existing, @NotNull ItemStack incoming) {
        return existing != null && existing.isSimilar(incoming);
    }

    /**
     * 钩子：此槽是否可直接放入新物品（默认：槽位为空/空气）。
     */
    protected boolean isAcceptableTarget(@Nullable ItemStack existing) {
        return existing == null || existing.isEmpty();
    }

    /**
     * 钩子：清理时是否应当归还该物品（默认：所有非空物品）。
     */
    protected boolean shouldReturn(@Nullable ItemStack item) {
        return item != null && !item.isEmpty();
    }

    /**
     * 默认清理：将本拼图槽位中的物品归还给所有者玩家。
     * 子类可通过覆写 shouldReturn 过滤背景等物品。
     */
    @Override
    public void cleanupOnClose() {
        List<ItemStack> toReturn = new ArrayList<>();
        Inventory inventory = gui.getInventory();
        for (int slot : this.getSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (shouldReturn(item)) {
                toReturn.add(item);
                inventory.setItem(slot, null);
            }
        }
        returnItemsToPlayer(gui.getOwner(), toReturn);
    }

    @Override
    public Consumer<C> getChangedCallBack() {
        return onContentsChanged;
    }

    @Override
    public boolean hasChangedCallBack() {
        return onContentsChanged != null;
    }
}

