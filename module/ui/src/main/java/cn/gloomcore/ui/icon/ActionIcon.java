package cn.gloomcore.ui.icon;

import cn.gloomcore.action.Action;
import org.bukkit.event.inventory.ClickType;

import java.util.EnumMap;

public class ActionIcon {
    protected final IconDisplay display;
    protected EnumMap<ClickType, Action> actions;

    public void addAction(ClickType type){
        actions.put(type, Action.);

    }

}
