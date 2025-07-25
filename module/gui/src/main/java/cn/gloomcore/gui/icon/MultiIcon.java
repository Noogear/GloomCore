package cn.gloomcore.gui.icon;

import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MultiIcon implements Icon {
    private List<BaseIcon> iconCache;
    private int index;


    @Override
    public ItemStack item(StringReplacer replacer) {
        if (index >= iconCache.size()) {
            return null;
        }
        return iconCache.get(index++).item(replacer);
    }


    @Override
    public Icon onClick(InventoryClickEvent event) {
        return null;
    }
}
