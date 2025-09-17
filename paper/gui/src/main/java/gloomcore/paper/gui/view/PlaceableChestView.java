package gloomcore.paper.gui.view;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.layout.ChestLayout;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.Puzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * GUI视图类，代表一个可交互的GUI界面
 * <p>
 * 该类管理GUI中的所有拼图(Puzzle)组件，处理事件并渲染界面内容。
 * 每个GUI视图都有一个唯一的“所有者” (owner)。所有的渲染、事件回调和状态变更都将以所有者的视角和名义进行，
 * 即使有多个玩家（观察者）可以同时查看此界面。
 */
public class PlaceableChestView<C extends Context> extends AbstractGui<C> {
    private final List<PlaceablePuzzle<C>> placeablePuzzles = new ArrayList<>();
    private final ChestLayout menuLayout;
    private long lastActionTime = 0L;

    /**
     * 构造一个新的GUI视图实例
     *
     * @param menuLayout 菜单布局定义
     */
    @SuppressWarnings("unchecked")
    public PlaceableChestView(ChestLayout menuLayout, Function<C, Component> title, C owner) {
        super(owner, title, (Puzzle<C>[]) new Puzzle<?>[menuLayout.getSize()]);
        this.menuLayout = menuLayout;
    }

    /**
     * 向GUI视图中添加一个拼图组件
     * <p>
     * 拼图组件会被添加到视图的拼图集合中，并根据其槽位分配到对应的槽位数组中。
     * 如果指定槽位已被占用，则抛出异常
     *
     * @param puzzle 要添加的拼图组件
     * @throws IllegalArgumentException 当槽位已被占用时抛出
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
     * 处理库存点击事件
     * <p>
     * 处理玩家在GUI中的点击操作，包括取消事件、处理拼图点击以及物品移动等操作
     * 请注意：所有由此产生的拼图回调（如 onClick, onChanged）都将传递 GUI 的所有者 (owner) 作为目标玩家，
     * 而非实际点击操作的玩家。如果需要操作者，请从 event.getWhoClicked() 获取。
     *
     * @param event 库存点击事件
     */
    @Override
    public void handleClick(InventoryClickEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastActionTime < 100L) {
            event.setCancelled(true);
            return;
        }
        this.lastActionTime = currentTime;
        if (inventory == null) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (inventory.equals(clickedInventory)) {
            event.setCancelled(true);
            findPuzzleBySlot(event.getRawSlot()).ifPresent(puzzle -> puzzle.onClick(event, owner));
            return;
        }

        if (clickedInventory != null) {
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
                    PaperScheduler.INSTANCE.entity(event.getWhoClicked()).runDelayed((task) -> {
                        for (PlaceablePuzzle<C> puzzle : puzzlesToUpdate) {
                            puzzle.getChangedCallBack().accept(owner);
                        }
                    }, 1L);
                }
            } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 处理库存拖拽事件
     * <p>
     * 处理玩家在GUI中的拖拽操作，验证拖拽目标槽位是否为可放置拼图，并在需要时更新相关拼图
     *
     * @param event 库存拖拽事件
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
                    @SuppressWarnings("unchecked")
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
     * 处理库存打开事件
     * <p>
     * 当前实现为空，可由子类重写以提供具体功能
     *
     * @param event 库存打开��件
     */
    @Override
    public void handleOpen(InventoryOpenEvent event) {
    }

    /**
     * 处理GUI关闭事件，清理所有可放置的拼图组件
     * <p>
     * 当GUI关闭时，此方法会遍历所有可放置的拼图(PlaceablePuzzle)，
     * 并调用它们的清理方法，确保正确处理放置在GUI中的物品
     *
     * @param event 库存关闭事件
     */
    @Override
    public void handleClose(InventoryCloseEvent event) {
        cleanupOnClose();

    }

    /**
     * 清理所有可放置拼图组件。
     * <p>
     * 遍历所有可放置拼图组件并调用其清理方法，这通常用于将放置在GUI中的物品返还给所有者(owner)。
     */
    @Override
    public void cleanupOnClose() {
        if (!placeablePuzzles.isEmpty()) {
            for (PlaceablePuzzle<C> placeablePuzzle : placeablePuzzles) {
                placeablePuzzle.cleanupOnClose(owner, inventory);
            }
        }
        if (inventory != null) {
            inventory.close();
        }
    }

    /**
     * 获取与此视图关联的库存对象
     *
     * @return 库存对象
     */
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, menuLayout.getSize(), parsedMenuTitle());
        }
        return inventory;
    }

}
