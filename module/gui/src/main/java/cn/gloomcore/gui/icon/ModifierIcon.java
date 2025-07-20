package cn.gloomcore.gui.icon;

import cn.gloomcore.action.Action;
import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.ReplacerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class ModifierIcon extends BaseIcon {
    protected final ItemModifier itemModifier;

    public ModifierIcon(ItemStack item, ItemModifier itemModifier, EnumMap<ClickType, Action<Player>> clickActions, ItemModifier itemModifier1) {
        super(item, clickActions);
        this.itemModifier = itemModifier;
    }

    public ModifierIcon(ItemStack item, ItemModifier itemModifier, ItemModifier itemModifier1) {
        this(item, itemModifier, new EnumMap<>(ClickType.class), itemModifier1);
    }

    public ModifierIcon(ItemStack item, ItemModifier itemModifier) {
        this(item, null, new EnumMap<>(ClickType.class), itemModifier);
    }

    public ModifierIcon(ItemModifier itemModifier) {
        this(DEFAULT_ITEM.clone(), null, new EnumMap<>(ClickType.class), itemModifier);
    }

    public ItemModifier getItemModifier() {
        return itemModifier;
    }

    public EnumMap<ClickType, Action<Player>> getClickActions() {
        return clickActions;
    }

    @Override
    public ItemStack item(ReplacerCache replacerCache) {
        if (itemModifier != null) {
            return itemModifier.modify(item.clone(), replacerCache);
        }
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
