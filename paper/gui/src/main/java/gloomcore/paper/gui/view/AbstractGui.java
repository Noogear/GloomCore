package gloomcore.paper.gui.view;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.puzzle.Puzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractGui<C extends Context> implements InventoryHolder {
    protected final List<Puzzle<C>> puzzles = new ArrayList<>();
    protected final C owner;
    protected final Function<C, Component> title;
    protected final Puzzle<C>[] slotPuzzleArray;
    protected @Nullable Inventory inventory;

    @SuppressWarnings("unchecked")
    protected AbstractGui(C owner, Function<C, Component> title, Puzzle<C>[] slotPuzzleArray) {
        this.owner = owner;
        this.title = title;
        this.slotPuzzleArray = slotPuzzleArray;
    }

    /**
     * 根据GUI所有者(owner)的视角，渲染所有拼图���件。
     * 例如，某些拼图可能会根据所有者的权限或数据显示不同的状态。
     */
    protected void renderAll() {
        puzzles.forEach(puzzle -> puzzle.render(owner, getInventory()));
    }

    public abstract void handleClick(InventoryClickEvent event);

    public abstract void handleDrag(InventoryDragEvent event);

    public abstract void handleOpen(InventoryOpenEvent event);

    public abstract void handleClose(InventoryCloseEvent event);

    public abstract void cleanupOnClose();

    /**
     * 解析菜单标题
     *
     * @return 菜单标题组件
     */
    protected Component parsedMenuTitle() {
        return title.apply(owner);
    }

    /**
     * 根据槽位查找对应的拼图组件
     *
     * @param slot 槽位索引
     * @return 包含拼图组件的Optional对象，如果未找到则为空
     */
    protected Optional<Puzzle<C>> findPuzzleBySlot(int slot) {
        if (slot >= 0 && slot < this.slotPuzzleArray.length) {
            return Optional.ofNullable(this.slotPuzzleArray[slot]);
        }
        return Optional.empty();
    }

    public C getOwner() {
        return owner;
    }

    /**
     * 为玩家打开此GUI视图
     * <p>
     * 如果GUI尚未渲染或为空，则先渲染所有拼图组件，
     * 然后在玩家的客户端打开GUI界面
     *
     * @param player 目标玩家
     */
    public void open(Player player) {
        if (inventory == null || inventory.isEmpty()) {
            renderAll();
        }
        if (!inventory.getViewers().contains(player)) {
            player.openInventory(inventory);
        }
    }

    /**
     * 关闭玩家当前打开的GUI界面
     * <p>
     * 该方法会检查指定玩家当前打开的界面是否为本GUI界面，如果是则关闭它。
     * </p>
     *
     * @param player 需要关闭GUI界面的玩家
     */
    public void close(Player player) {
        if (inventory != null && inventory.equals(player.getOpenInventory().getTopInventory())) {
            player.closeInventory();
        }
    }

    /**
     * 异步打开GUI视图
     * <p>
     * 在异步线程中渲染GUI内容，然后在实体线程中打开GUI给玩家
     * 这样可以避免在主线程中执行耗时的渲染操作，提高性能
     *
     * @param player 目标玩家
     * @return CompletableFuture对象，可用于链式调用或等待操作完成
     */
    public CompletableFuture<Void> openAsync(Player player) {
        return CompletableFuture
                .runAsync(() -> {
                    if (inventory == null || inventory.isEmpty()) {
                        renderAll();
                    }
                }, PaperScheduler.INSTANCE.async().executor())
                .thenAcceptAsync(result -> {
                    if (inventory != null && !inventory.getViewers().contains(player)) {
                        player.openInventory(inventory);
                    }
                }, PaperScheduler.INSTANCE.entity(player).executor());
    }


}
