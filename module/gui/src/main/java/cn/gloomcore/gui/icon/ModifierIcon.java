package cn.gloomcore.gui.icon;

import cn.gloomcore.action.Action;
import cn.gloomcore.item.ItemModifier;
import cn.gloomcore.replacer.StringReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;

public class ModifierIcon extends BaseIcon {
    protected final ItemModifier itemModifier;

    public ModifierIcon(ItemStack item, ItemModifier itemModifier) {
        super(item);
        this.itemModifier = itemModifier;
    }

    public ItemModifier getItemModifier() {
        return itemModifier;
    }

    public EnumMap<ClickType, Action<Player>> getClickActions() {
        return clickActions;
    }

    @Override
    public ItemStack item(StringReplacer replacer) {
        if (itemModifier != null) {
            return itemModifier.modify(item.clone(), replacer);
        }
        return item.clone();
    }


}
