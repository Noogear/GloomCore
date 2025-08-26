package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.IconDisplay;
import cn.gloomcore.ui.puzzle.abstracts.StaticPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class StaticItemsPuzzleImpl extends StaticPuzzle {
    private final IconDisplay display;

    public StaticItemsPuzzleImpl(Collection<Integer> slotList, IconDisplay display) {
        super(slotList);
        this.display = display;
    }

    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        ItemStack itemStack = display.parse(player);
        for (int slot : slots) {
            inventory.setItem(slot, itemStack);
        }
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }
}
