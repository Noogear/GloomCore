package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.abstracts.PlaceableBasePuzzle;
import gloomcore.paper.gui.view.AbstractGui;
import gloomcore.paper.scheduler.PaperScheduler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class BackgroundPlaceablePuzzleImpl<C extends Context> extends PlaceableBasePuzzle<C> implements PlaceablePuzzle<C> {
    private final ItemStack backgroundItem;

    public BackgroundPlaceablePuzzleImpl(@NotNull Collection<Integer> slotList,
                                         @NotNull ItemStack backgroundItem,
                                         @Nullable Consumer<C> onContentsChanged,
                                         boolean stackingEnabled,
                                         AbstractGui<C> gui) {
        super(slotList, onContentsChanged, stackingEnabled, gui);
        this.backgroundItem = backgroundItem;
    }

    public BackgroundPlaceablePuzzleImpl(@NotNull BackgroundPlaceablePuzzleImpl<C> other) {
        super(other);
        this.backgroundItem = other.backgroundItem.clone();
    }

    public BackgroundPlaceablePuzzleImpl(@NotNull BackgroundPlaceablePuzzleImpl<C> other, @NotNull AbstractGui<C> gui) {
        super(other, gui);
        this.backgroundItem = other.backgroundItem.clone();
    }

    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, backgroundItem.clone());
        }
    }

    @Override
    public void onClick(InventoryClickEvent event, C owner) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (cursor != null && !cursor.isEmpty()) {
            event.setCancelled(false);
        } else if (current != null && current.isSimilar(backgroundItem)) {
            event.setCancelled(true);
            return;
        } else {
            event.setCancelled(false);
        }
        PaperScheduler.INSTANCE.entity(event.getWhoClicked()).runDelayed(() -> {
            ItemStack after = event.getInventory().getItem(event.getSlot());
            if (after == null || after.getType().isAir()) {
                event.getInventory().setItem(event.getSlot(), backgroundItem.clone());
            }
            if (onContentsChanged != null) {
                onContentsChanged.accept(owner);
            }
        }, 1L);
    }

    @Override
    protected boolean canStackWithExisting(@Nullable ItemStack existing, @NotNull ItemStack incoming) {
        return existing != null && !existing.isSimilar(backgroundItem) && existing.isSimilar(incoming);
    }

    @Override
    protected boolean isAcceptableTarget(@Nullable ItemStack existing) {
        return existing == null || existing.isEmpty() || existing.isSimilar(backgroundItem);
    }

    @Override
    protected boolean shouldReturn(@Nullable ItemStack item) {
        return item != null && !item.isEmpty() && !item.isSimilar(backgroundItem);
    }
}
