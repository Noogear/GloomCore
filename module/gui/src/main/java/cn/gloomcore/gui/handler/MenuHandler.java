package cn.gloomcore.gui.handler;

import cn.gloomcore.gui.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof Menu menu))
            return;
        menu.onClick(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof Menu menu))
            return;
        menu.onDrag(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof Menu menu))
            return;
        menu.onOpen(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder(false);
        if (!(holder instanceof Menu menu))
            return;
        menu.onClose(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder(false);
        if (!(holder instanceof Menu menu))
            return;
        menu.onQuit(event);
    }
}
