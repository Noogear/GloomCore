package gloomcore.paper.gui.view;

import gloomcore.paper.contract.Context;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractGui<C extends Context<Player>> implements InventoryHolder {
    protected final List<Puzzle<C>> puzzles = new ArrayList<>();
    protected final C owner;
    protected final Function<C, Component> title;
    protected final Puzzle<C>[] slotPuzzleArray;
    protected @Nullable Inventory inventory;

    protected AbstractGui(C owner, Function<C, Component> title, Puzzle<C>[] slotPuzzleArray) {
        this.owner = owner;
        this.title = title;
        this.slotPuzzleArray = slotPuzzleArray;
    }

    /**
     * 根据 GUI 所有者的视角渲染所有拼图组件。
     * 某些拼图可能会根据所有者的权限或数据显示不同的状态。
     */
    protected void renderAll() {
        puzzles.forEach(puzzle -> puzzle.render(owner, getInventory()));
    }

    public void handleClick(InventoryClickEvent event) {
        if (inventory == null) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (inventory.equals(clickedInventory)) {
            event.setCancelled(true);
            findPuzzleBySlot(event.getRawSlot()).ifPresent(puzzle -> puzzle.onClick(event, owner));
            return;
        }
        if (clickedInventory != null) {
            handlePlayerInventoryClick(event);
        }
    }

    public abstract void handlePlayerInventoryClick(InventoryClickEvent event);

    public abstract void handleDrag(InventoryDragEvent event);

    public abstract void handleOpen(InventoryOpenEvent event);

    public abstract void handleClose(InventoryCloseEvent event);

    public abstract void cleanupOnClose();

    /**
     * 解析菜单标题。
     *
     * @return 菜单标题组件。
     */
    protected Component parsedMenuTitle() {
        return title.apply(owner);
    }

    /**
     * 根据槽位查找对应的拼图组件。
     *
     * @param slot 槽位索引。
     * @return 包含拼图组件的 Optional 对象，如果未找到则为空。
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
     * 为玩家打开此 GUI 视图。
     * 如果 GUI 尚未渲染或为空，则先渲染所有拼图组件，然后在玩家客户端打开。
     *
     * @param player 目标玩家。
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
     * 关闭玩家当前打开的 GUI 视图。
     * 会检查指定玩家当前打开的界面是否为本 GUI，如果是则关闭。
     *
     * @param player 需要关闭 GUI 的玩家。
     */
    public void close(Player player) {
        if (inventory != null && inventory.equals(player.getOpenInventory().getTopInventory())) {
            player.closeInventory();
        }
    }

    /**
     * 异步打开 GUI 视图。
     * 在异步线程中渲染内容，随后在实体线程为玩家打开，以避免主线程阻塞。
     *
     * @param player 目标玩家。
     * @return CompletableFuture 对象，可用于链式调用或等待操作完成。
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
