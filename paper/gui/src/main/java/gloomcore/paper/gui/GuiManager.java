package gloomcore.paper.gui;

import gloomcore.paper.gui.view.AbstractGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI管理器类，用于管理玩家的GUI界面导航和历史记录
 * <p>
 * 该类负责处理玩家打开GUI、返回上一级GUI、以及清理玩家数据等操作，
 * 同时监听相关事件并转发给对应的GUI视图处理
 */
public class GuiManager implements Listener {
    private final ConcurrentHashMap<UUID, Deque<AbstractGui>> history = new ConcurrentHashMap<>();
    private final Set<UUID> navigatingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * 构造一个新的GUI管理器实例，并注册事件监听器
     *
     * @param plugin 插件实例，用于注册事件监听器
     */
    public GuiManager(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void disable() {
        HandlerList.unregisterAll(this);
    }

    /**
     * 为玩家打开一个新的GUI，并将当前GUI（如果有）存入历史记录。
     *
     * @param player       玩家
     * @param view         要打开的GUI
     * @param storeHistory 是否将当前打开的GUI存入历史
     */
    public void open(Player player, AbstractGui view, boolean storeHistory) {
        if (storeHistory && player.getOpenInventory().getTopInventory().getHolder(false) instanceof AbstractGui currentHolder) {
            history.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>()).push(currentHolder);
        }
        navigatingPlayers.add(player.getUniqueId());
        view.open(player);
    }

    /**
     * 为玩家打开一个新GUI，默认不存储历史（适用于打开主菜单等场景）。
     *
     * @param player 玩家
     * @param view   要打开的GUI视图
     */
    public void open(Player player, AbstractGui view) {
        history.remove(player.getUniqueId());
        navigatingPlayers.add(player.getUniqueId());
        view.open(player);
    }

    /**
     * 返回上一页。
     *
     * @param player 玩家
     * @return 如果成功返回，返回true
     */
    public boolean back(Player player) {
        Deque<AbstractGui> playerHistory = history.get(player.getUniqueId());
        if (playerHistory != null && !playerHistory.isEmpty()) {
            AbstractGui previousView = playerHistory.pop();
            navigatingPlayers.add(player.getUniqueId());
            previousView.open(player);
            return true;
        }
        return false;
    }

    /**
     * 处理库存点击事件，将事件转发给对应的GUI视图进行处理
     *
     * @param event 库存点击事件
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof AbstractGui gui)) {
            return;
        }
        gui.handleClick(event);
    }

    /**
     * 处理玩家退出事件，清理该玩家的历史记录和导航状态
     *
     * @param event 玩家退出事件
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        navigatingPlayers.remove(playerUuid);
        Deque<AbstractGui> playerHistory = history.remove(playerUuid);
        if (playerHistory == null || playerHistory.isEmpty()) {
            return;
        }
        for (AbstractGui historicalView : playerHistory) {
            if (playerUuid.equals(historicalView.getOwner().getUniqueId())) {
                historicalView.cleanupOnClose();
            }
        }
    }

    /**
     * 处理库存关闭事件，根据关闭原因决定是否清理玩家的历史记录
     *
     * @param event 库存关闭事件
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof AbstractGui closedView)) {
            return;
        }
        UUID playerUuid = event.getPlayer().getUniqueId();
        if (navigatingPlayers.remove(playerUuid)) {
            return;
        }
        history.remove(playerUuid);
        if (playerUuid.equals(closedView.getOwner().getUniqueId())) {
            closedView.handleClose(event);
        }
    }

    /**
     * 处理库存拖拽事件，将事件转发给对应的GUI视图进行处理
     *
     * @param event 库存拖拽事件
     */
    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof AbstractGui gui)) {
            return;
        }
        gui.handleDrag(event);
    }


}
