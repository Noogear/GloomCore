package cn.gloomcore.gui.menu;

import cn.gloomcore.gui.icon.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface Menu extends InventoryHolder {

    Component title();

    void open();

    void close();

    Player getOwner();

    Icon onClick(InventoryClickEvent event);

    void onDrag(InventoryDragEvent event);

    void onOpen(InventoryOpenEvent event);

    void onClose(InventoryCloseEvent event);

    Menu openMenu();

    ItemStack getItemStack(int slot);

    boolean isEmpty(int slot);

    void remove(int slot);

    void clear();

}
