package cn.gloomcore.gui.icon;

import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MultiIcon implements Icon {
    private List<BaseIcon> iconCache;
    private int index;


    @Override
    public ItemStack item(ReplacerCache replacerCache) {
        if (index >= iconCache.size()) {
            return null;
        }
        return iconCache.get(index++).item(replacerCache);
    }


    @Override
    public Icon onClick(InventoryClickEvent event) {
        return null;
    }
}
