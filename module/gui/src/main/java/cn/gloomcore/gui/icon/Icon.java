package cn.gloomcore.gui.icon;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Icon {
    ItemStack item(StringReplacer replacer);

    default ItemStack item() {
        return item(null);
    }

    Icon onClick(InventoryClickEvent event);
}
