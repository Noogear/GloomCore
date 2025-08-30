package gloomcore.paper.gui;

import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.Puzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * GUI视图类，代表一个可交互的GUI界面
 * <p>
 * 该类管理GUI中的所有拼图(Puzzle)组件，处理事件并渲染界面内容。
 * 每个GUI视图都与特定的玩家相关联，并包含一个菜单布局定义
 */
public class PuzzleGuiView implements InventoryHolder {
    private final List<Puzzle> puzzles = new ArrayList<>();
    private final List<PlaceablePuzzle> placeablePuzzles = new ArrayList<>();
    private final Puzzle[] slotPuzzleArray;
    private final MenuLayout menuLayout;
    private final Function<Player, Component> title;
    private final Player owner;

    private @Nullable Inventory inventory;

    /**
     * 构造一个新的GUI视图实例
     *
     * @param menuLayout 菜单布局定义
     */
    public PuzzleGuiView(MenuLayout menuLayout, Function<Player, Component> title, Player owner) {
        this.menuLayout = menuLayout;
        this.slotPuzzleArray = new Puzzle[menuLayout.getSize()];
        this.title = title;
        this.owner = owner;
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
    protected void addPuzzle(Puzzle puzzle) {
        this.puzzles.add(puzzle);
        for (int slot : puzzle.getSlots()) {
            if (slot >= 0 && slot < this.slotPuzzleArray.length) {
                if (this.slotPuzzleArray[slot] != null) {
                    throw new IllegalArgumentException("Slot " + slot + " is already occupied!");
                }
                this.slotPuzzleArray[slot] = puzzle;
            }
        }
        if (puzzle instanceof PlaceablePuzzle placeablePuzzle) {
            this.placeablePuzzles.add(placeablePuzzle);
        }

    }

    /**
     * 渲染所有拼图组件到玩家的库存中
     */
    private void renderAll() {
        puzzles.forEach(puzzle -> puzzle.render(owner, getInventory()));
    }


    /**
     * 处理库存点击事件
     * <p>
     * 处理玩家在GUI中的点击操作，包括取消事件、处理拼图点击以及物品移动等操作
     *
     * @param event 库存点击事件
     */
    public void handleClick(InventoryClickEvent event) {
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
                Set<PlaceablePuzzle> puzzlesToUpdate = new ObjectOpenHashSet<>();
                for (PlaceablePuzzle puzzle : this.placeablePuzzles) {
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
                        for (PlaceablePuzzle puzzle : puzzlesToUpdate) {
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
    public void handleDrag(InventoryDragEvent event) {
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
            Set<PlaceablePuzzle> puzzlesToUpdate = new ObjectOpenHashSet<>();
            for (int slot : event.getRawSlots()) {
                if (slot < size) {
                    PlaceablePuzzle puzzle = (PlaceablePuzzle) slotPuzzleArray[slot];
                    if (puzzle.hasChangedCallBack()) {
                        puzzlesToUpdate.add(puzzle);
                    }
                }
            }
            if (puzzlesToUpdate.isEmpty()) {
                task.cancel();
                return;
            }
            for (PlaceablePuzzle puzzle : puzzlesToUpdate) {
                puzzle.getChangedCallBack().accept(owner);
            }
        }, 1L);
    }

    /**
     * 处理库存打开事件
     * <p>
     * 当前实现为空，可由子类重写以提供具体功能
     *
     * @param event 库存打开事件
     */
    public void handleOpen(InventoryOpenEvent event) {
    }

    /**
     * 处理GUI关闭事件，清理所有可放置的拼图组件
     * <p>
     * 当GUI关闭时，此方法会遍历所有可放置的拼图(PlaceablePuzzle)，
     * 并调用它们的清理方法，确保正确处理放置在GUI中的物品
     */
    public void handleClose() {
        if (!placeablePuzzles.isEmpty()) {
            for (PlaceablePuzzle placeablePuzzle : placeablePuzzles) {
                placeablePuzzle.cleanupOnClose(owner, inventory);
            }
        }
        if (inventory != null) {
            inventory.close();
        }
    }

    /**
     * 解析菜单标题
     *
     * @return 菜单标题组件
     */
    public Component parsedMenuTitle() {
        return title.apply(owner);
    }


    /**
     * 根据槽位查找对应的拼图组件
     *
     * @param slot 槽位索引
     * @return 包含拼图组件的Optional对象，如果未找到则为空
     */
    private Optional<Puzzle> findPuzzleBySlot(int slot) {
        if (slot >= 0 && slot < this.slotPuzzleArray.length) {
            return Optional.ofNullable(this.slotPuzzleArray[slot]);
        }
        return Optional.empty();
    }

    public Player getOwner() {
        return owner;
    }

    /**
     * 为玩家打开此GUI视图
     *
     * @param player 目标玩家
     */
    public void open(Player player) {
        if (inventory == null || inventory.isEmpty()) {
            renderAll();
        }
        player.openInventory(inventory);
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
