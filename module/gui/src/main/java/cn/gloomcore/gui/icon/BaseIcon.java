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
    public ItemStack item() {
        return item.clone();
    }

    @Override
    public Icon onClick(ClickType clickType, Player player) {
        Action<Player> action = clickActions.get(clickType);
        if(action != null){
            action.run(player);
        }
        return this;
    }}
