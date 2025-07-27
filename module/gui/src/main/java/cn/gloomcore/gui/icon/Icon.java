package cn.gloomcore.gui.icon;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Icon {

    ItemStack item();

    default ItemStack item(StringReplacer replacer) {
        return item();
    }

    default Icon onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        return this;
    }

}
