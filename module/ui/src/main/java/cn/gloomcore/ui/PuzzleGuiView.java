package cn.gloomcore.ui;

import cn.gloomcore.ui.puzzle.PlaceablePuzzle;
import cn.gloomcore.ui.puzzle.Puzzle;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private final JavaPlugin plugin;

    private @Nullable Inventory inventory;

    /**
     * 构造一个新的GUI视图实例
     *
     * @param menuLayout 菜单布局定义
     */
    public PuzzleGuiView(MenuLayout menuLayout, @NotNull JavaPlugin plugin) {
        this.menuLayout = menuLayout;
        this.slotPuzzleArray = new Puzzle[menuLayout.getSize()];
        this.plugin = plugin;
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
     *
     * @param player 目标玩家
     */
    private void renderAll(Player player) {
        puzzles.forEach(puzzle -> puzzle.render(player, getInventory()));
    }


    public void handleClick(InventoryClickEvent event) {
        if (inventory == null) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (inventory.equals(clickedInventory)) {
            event.setCancelled(true);
            findPuzzleBySlot(event.getRawSlot()).ifPresent(puzzle -> puzzle.onClick(event));
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
                for (PlaceablePuzzle puzzle : this.placeablePuzzles) {
                    puzzle.tryAcceptItem(itemToMove, this.getInventory());
                    if (itemToMove.getAmount() <= 0) {
                        event.setCurrentItem(null);
                        break;
                    }
                }
            } else if (action == InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
        }
    }

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
        Player player = (Player) event.getWhoClicked();
        player.getScheduler().runDelayed(plugin, (task) -> {
            Set<PlaceablePuzzle> puzzlesToUpdate = new ObjectOpenHashSet<>();
            for (int slot : event.getRawSlots()) {
                if (slot < size) {
                    puzzlesToUpdate.add((PlaceablePuzzle) slotPuzzleArray[slot]);
                }
                if (puzzlesToUpdate.isEmpty()) {
                    task.cancel();
                    return;
                }
                for (PlaceablePuzzle puzzle : puzzlesToUpdate) {
                    puzzle.onContentsChanged().accept(player);
                }
            }

        }, null, 1L);


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


    public void handleClose(Player player) {
        if (placeablePuzzles.isEmpty()) {
            return;
        }
        for (PlaceablePuzzle placeablePuzzle : placeablePuzzles) {
            placeablePuzzle.cleanupOnClose(player, this.getInventory());
        }
    }

    /**
     * 解析菜单标题
     *
     * @return 菜单标题组件
     */
    public Component parsedMenuTitle() {
        return menuLayout.getInventoryType().defaultTitle();
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

    /**
     * 为玩家打开此GUI视图
     *
     * @param player 目标玩家
     */
    public void open(Player player) {
        renderAll(player);
        player.openInventory(this.getInventory());
    }

    /**
     * 获取与此视图关联的库存对象
     *
     * @return 库存对象
     */
    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            return inventory = Bukkit.createInventory(this, menuLayout.getInventoryType());
        }
        return inventory;
    }
}
