package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.abstracts.PlaceableBasePuzzle;
import gloomcore.paper.gui.view.AbstractGui;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 可放置槽位拼图：提供一组空槽位，允许玩家放置/取出物品。
 * 交互、清理与搬运逻辑复用 PlaceableBasePuzzle。
 */
public class PlaceablePuzzleImpl<C extends Context> extends PlaceableBasePuzzle<C> implements PlaceablePuzzle<C> {

    public PlaceablePuzzleImpl(@NotNull Collection<Integer> slotList,
                               @Nullable Consumer<C> onContentsChanged,
                               boolean stackingEnabled,
                               AbstractGui<C> gui) {
        super(slotList, onContentsChanged, stackingEnabled, gui);
    }

    public PlaceablePuzzleImpl(@NotNull PlaceablePuzzleImpl<C> other) {
        super(other);
    }

    public PlaceablePuzzleImpl(@NotNull PlaceablePuzzleImpl<C> other, @NotNull AbstractGui<C> gui) {
        super(other, gui);
    }

    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, null);
        }
    }
}
