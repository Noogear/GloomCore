package cn.gloomcore.gui;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseGui extends AbstractGui implements Listener {
    private @Nullable Inventory inventoryCache;
    private int size;

    @Override
    public @NotNull Inventory getInventory() {
        Inventory inventory;
        if (inventoryCache == null) {
            inventory = Bukkit.createInventory(this, size, title());
        } else {
            inventory = inventoryCache;
        }
        return inventory;
    }

    public int getSize() {
        return size;
    }


}
