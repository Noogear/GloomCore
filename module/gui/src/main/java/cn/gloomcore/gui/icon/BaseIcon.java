package cn.gloomcore.gui.icon;

import cn.gloomcore.action.Action;
import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class BaseIcon implements Icon {
    protected final ItemStack item;
    protected final EnumMap<ClickType, Action<Player>> clickActions;

    public BaseIcon(ItemStack item, EnumMap<ClickType, Action<Player>> clickActions) {
        this.item = item;
        this.clickActions = clickActions;
    }

    public BaseIcon() {
        this(DEFAULT_ITEM.clone(), null);
    }

    public ItemStack getOriginalItem() {
        return item;
    }

    @Override
    public ItemStack item(ReplacerCache replacerCache) {
        return item.clone();
    }

    @Override
    public Icon onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        return this;
    }
}
