package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.IconDisplay;
import cn.gloomcore.ui.puzzle.StaticPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class StaticItemsPuzzle extends StaticPuzzle {

    private final Map<Integer, IconDisplay> items;

    public StaticItemsPuzzle(Map<Integer, IconDisplay> items) {
        this.items = items;
    }

    @Override
    public Set<Integer> getSlots() {
        return items.keySet();
    }

    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        items.forEach((slot, display) -> inventory.setItem(slot, display.parse()));
    }
}
