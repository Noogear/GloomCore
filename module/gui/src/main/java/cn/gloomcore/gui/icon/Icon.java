package cn.gloomcore.gui.icon;

import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Icon {
    ItemStack item(ReplacerCache replacerCache);

    default ItemStack item() {
        return item(null);
    }

    Icon onClick(InventoryClickEvent event);
}
