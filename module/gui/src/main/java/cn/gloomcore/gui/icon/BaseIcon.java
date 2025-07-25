package cn.gloomcore.gui.icon;

import cn.gloomcore.action.Action;
import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class BaseIcon implements Icon {
    protected final ItemStack item;
    protected final EnumMap<ClickType, Action<Player>> clickActions = new EnumMap<>(ClickType.class);

    public BaseIcon(ItemStack item) {
        this.item = item;
    }

    public ItemStack getOriginalItem() {
        return item;
    }

    @Override
    public ItemStack item(StringReplacer replacer) {
        return item.clone();
    }

    @Override
    public Icon onClick(InventoryClickEvent event) {
        Action<Player> action = clickActions.get(event.getClick());
        if (action != null) {
            if (event.getWhoClicked() instanceof Player player) {
                action.run(player);
            }
        }
        return this;
    }
}
