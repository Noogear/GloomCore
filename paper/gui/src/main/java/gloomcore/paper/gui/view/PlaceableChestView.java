package gloomcore.paper.gui.view;

import gloomcore.paper.contract.Context;
import gloomcore.paper.gui.layout.ChestLayout;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.Puzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 可放置类的通用箱型 GUI 视图。
 * <p>
 * 管理拼图（Puzzle）的渲染与交互，将事件分发给对应拼图；
 * 同时在 GUI 关闭时负责触发可放置拼图的清理逻辑。
 */
public class PlaceableChestView<C extends Context<Player>> extends AbstractGui<C> {
    private final List<PlaceablePuzzle<C>> placeablePuzzles = new ArrayList<>();
    private final ChestLayout menuLayout;
    private long lastActionTime = 0L;

    /**
     * 使用给定布局与标题构造视图。
     */
    @SuppressWarnings("unchecked")
    public PlaceableChestView(ChestLayout menuLayout, Function<C, Component> title, C owner) {
        super(owner, title, (Puzzle<C>[]) new Puzzle<?>[menuLayout.getSize()]);
        this.menuLayout = menuLayout;
    }

    /**
     * 添加一个拼图并占据相应槽位。
     * 若槽位已被占用将抛出异常。
     */
    public PlaceableChestView<C> addPuzzle(Puzzle<C> puzzle) {
        this.puzzles.add(puzzle);
        for (int slot : puzzle.getSlots()) {
            if (slot >= 0 && slot < this.slotPuzzleArray.length) {
                if (this.slotPuzzleArray[slot] != null) {
                    throw new IllegalArgumentException("Slot " + slot + " is already occupied!");
                }
                this.slotPuzzleArray[slot] = puzzle;
            }
        }
        if (puzzle instanceof PlaceablePuzzle<C> placeablePuzzle) {
            this.placeablePuzzles.add(placeablePuzzle);
        }
        return this;
    }

    public ChestLayout getMenuLayout() {
        return menuLayout;
    }

    /**
     * 处理库存点击事件（两类）：
     * 1) GUI 内部点击 -> 定位拼图并转发；
     * 2) 玩家背包点击 -> 尝试迁移物品至可放置槽位。
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastActionTime < 100L) {
            event.setCancelled(true);
            return;
        }
        this.lastActionTime = currentTime;
        super.handleClick(event);
    }

    /**
     * 处理玩家背包区域的物品点击（如 Shift+点击搬运）。
     */
    @Override
    public void handlePlayerInventoryClick(InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            ItemStack itemToMove = event.getCurrentItem();
            if (itemToMove == null || itemToMove.isEmpty()) {
                return;
            }
            Set<PlaceablePuzzle<C>> puzzlesToUpdate = new ObjectOpenHashSet<>();
            for (PlaceablePuzzle<C> puzzle : this.placeablePuzzles) {
                if (puzzle.tryAcceptItem(itemToMove, this.getInventory())) {
                    if (puzzle.hasChangedCallBack()) {
                        puzzlesToUpdate.add(puzzle);
                    }
                }
                if (itemToMove.getAmount() <= 0) {
                    event.setCurrentItem(null);
                    break;
                }
            }
            if (!puzzlesToUpdate.isEmpty()) {
                PaperScheduler.INSTANCE.entity(event.getWhoClicked()).runDelayed((_) -> {
                    for (PlaceablePuzzle<C> puzzle : puzzlesToUpdate) {
                        puzzle.getChangedCallBack().accept(owner);
                    }
                }, 1L);
            }
        } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }

    /**
     * 处理 GUI 内拖拽事件：仅允许拖拽到可放置拼图槽位。
     */
    @Override
    public void handleDrag(InventoryDragEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastActionTime < 100L) {
            event.setCancelled(true);
            return;
        }
        this.lastActionTime = currentTime;
        int size = menuLayout.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < size) {
                if (!(this.slotPuzzleArray[slot] instanceof PlaceablePuzzle)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        PaperScheduler.INSTANCE.entity(event.getWhoClicked()).runDelayed((task) -> {
            Set<PlaceablePuzzle<C>> puzzlesToUpdate = new ObjectOpenHashSet<>();
            for (int slot : event.getRawSlots()) {
                if (slot < size) {
                    PlaceablePuzzle<C> puzzle = (PlaceablePuzzle<C>) slotPuzzleArray[slot];
                    if (puzzle.hasChangedCallBack()) {
                        puzzlesToUpdate.add(puzzle);
                    }
                }
            }
            if (puzzlesToUpdate.isEmpty()) {
                task.cancel();
                return;
            }
            for (PlaceablePuzzle<C> puzzle : puzzlesToUpdate) {
                puzzle.getChangedCallBack().accept(owner);
            }
        }, 1L);
    }

    /**
     * 处理库存打开事件（子类可扩展）。
     */
    @Override
    public void handleOpen(InventoryOpenEvent event) {
    }

    /**
     * 处理 GUI 关闭事件并触发清理。
     */
    @Override
    public void handleClose(InventoryCloseEvent event) {
        cleanupOnClose();

    }

    /**
     * 清理所有可放置拼图：返还玩家物品并关闭库存。
     */
    @Override
    public void cleanupOnClose() {
        if (!placeablePuzzles.isEmpty()) {
            for (PlaceablePuzzle<C> placeablePuzzle : placeablePuzzles) {
                placeablePuzzle.cleanupOnClose();
            }
        }
        if (inventory != null) {
            inventory.close();
        }
    }

    /**
     * 获取与此视图关联的库存对象。
     */
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, menuLayout.getSize(), parsedMenuTitle());
        }
        return inventory;
    }

}
