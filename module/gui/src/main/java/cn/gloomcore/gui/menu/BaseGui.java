package cn.gloomcore.gui.menu;

import cn.gloomcore.gui.icon.Icon;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public class BaseGui extends AbstractGui {

    public BaseGui(Function<Player, Component> title, Map<Integer, Icon> slotMap) {
        super(title, slotMap);
    }


    @Override
    public void onDrag(InventoryDragEvent event) {

    }

    @Override
    public void onOpen(InventoryOpenEvent event) {

    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

    @Override
    public Menu openMenu() {
        return null;
    }

    @Override
    public void remove(int slot) {

    }

    @Override
    public void clear() {

    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
