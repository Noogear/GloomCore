package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.puzzle.PlaceablePuzzle;
import cn.gloomcore.ui.puzzle.Puzzle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 可放置槽位拼图类，提供一组可供玩家放置物品的空槽位
 * <p>
 * 该拼图在指定槽位中创建空位，允许玩家将物品放置在这些槽位中，
 * 并能监听内容变化事件。主要用于制作需要玩家放置物品的界面，
 * 如合成界面、物品选择界面等
 */
public class PlaceableSlotsPuzzle implements Puzzle, PlaceablePuzzle {
    private final JavaPlugin plugin;
    private final Set<Integer> slots;
    private final Consumer<Player> onContentsChanged;
    private final boolean stackingEnabled;


    public PlaceableSlotsPuzzle(@NotNull JavaPlugin plugin, @NotNull Set<Integer> slots, @Nullable Consumer<Player> onContentsChanged, boolean stackingEnabled) {
        this.plugin = plugin;
        this.slots = slots;
        this.onContentsChanged = onContentsChanged;
        this.stackingEnabled = stackingEnabled;
    }

    /**
     * 获取拼图占据的所有槽位
     *
     * @return 包含所有槽位索引的集合
     */
    @Override
    public Collection<Integer> getSlots() {
        return slots;
    }

    /**
     * 渲染拼图内容到指定库存中
     * <p>
     * 将所有指定槽位清空，使其变为空槽位供玩家放置物品
     *
     * @param player    目标玩家
     * @param inventory 目标库存
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        slots.forEach(slot -> inventory.setItem(slot, null));
    }

    /**
     * 处理槽位点击事件
     * <p>
     * 允许玩家放置或取出物品，并在内容变化后触发回调函数
     *
     * @param event 库存点击事件
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(false);
        if (onContentsChanged != null) {
            Player player = (Player) event.getWhoClicked();
            player.getScheduler().runDelayed(plugin, (task) -> onContentsChanged.accept(player), null, 1L);
        }

    }

    /**
     * 更新拼图显示内容
     * <p>
     * 此拼图为静态类型，不需要更新操作
     *
     * @param player 目标玩家
     */
    @Override
    public void update(Player player) {
    }

    @Override
    public void cleanupOnClose(Player player, Inventory inventory) {
        List<ItemStack> itemsToReturn = new ArrayList<>();
        for (int slot : this.getSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                itemsToReturn.add(item);
                inventory.setItem(slot, null);
            }
        }

        if (itemsToReturn.isEmpty()) {
            return;
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        Inventory playerInventory = player.getInventory();
        for (ItemStack item : itemsToReturn) {
            if (!playerInventory.addItem(item).isEmpty()) {
                world.dropItem(location, item);
            }

        }
    }

    @Override
    public boolean tryAcceptItem(ItemStack itemToAccept, Inventory inventory) {
        if (itemToAccept == null || itemToAccept.getAmount() <= 0) {
            return false;
        }
        int initialAmount = itemToAccept.getAmount();

        // 阶段1: 尝试堆叠
        if (this.stackingEnabled) {
            for (int slot : this.slots) {
                ItemStack existingItem = inventory.getItem(slot);
                if (existingItem != null && !existingItem.getType().isAir() && existingItem.isSimilar(itemToAccept)) {
                    int space = existingItem.getMaxStackSize() - existingItem.getAmount();
                    if (space > 0) {
                        int amountToMove = Math.min(space, itemToAccept.getAmount());
                        existingItem.add(amountToMove);
                        itemToAccept.subtract(amountToMove);
                    }
                }
                if (itemToAccept.getAmount() <= 0) {
                    return true;
                }
            }
        }

        if (itemToAccept.getAmount() > 0) {
            for (int slot : this.slots) {
                ItemStack slotItem = inventory.getItem(slot);
                if (slotItem == null || slotItem.getType().isAir()) {
                    inventory.setItem(slot, itemToAccept.clone());
                    itemToAccept.setAmount(0);
                    return true;
                }
            }
        }

        return itemToAccept.getAmount() < initialAmount;
    }

    @Override
    public Consumer<Player> getChangedCallBack() {
        return this.onContentsChanged;
    }

    @Override
    public boolean hasChangedCallBack() {
        return this.onContentsChanged != null;
    }


}
