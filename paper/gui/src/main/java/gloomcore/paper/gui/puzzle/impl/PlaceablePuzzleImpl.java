package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.puzzle.PlaceablePuzzle;
import gloomcore.paper.gui.puzzle.abstracts.AbstractPuzzle;
import gloomcore.paper.scheduler.PaperScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可放置槽位拼图类，提供一组可供玩家放置物品的空槽位
 * <p>
 * 该拼图在指定槽位中创建空位，允许玩家将物品放置在这些槽位中，
 * 并能监听内容变化事件。主要用于制作需要玩家放置物品的界面，
 * 如合成界面、物品选择界面等
 */
public class PlaceablePuzzleImpl extends AbstractPuzzle implements PlaceablePuzzle {
    private final Consumer<Player> onContentsChanged;
    private final boolean stackingEnabled;


    /**
     * 构造函数，创建一个可放置物品的拼图实例
     *
     * @param slotList          拼图占据的槽位列表
     * @param onContentsChanged 内容变更时的回调函数，可为null
     * @param stackingEnabled   是否启用物品堆叠功能
     */
    public PlaceablePuzzleImpl(@NotNull Collection<Integer> slotList, @Nullable Consumer<Player> onContentsChanged, boolean stackingEnabled) {
        super(slotList);
        this.onContentsChanged = onContentsChanged;
        this.stackingEnabled = stackingEnabled;
    }

    /**
     * 拷贝构造函数，基于另一个PlaceablePuzzleImpl实例创建新实例
     * <p>
     * 该构造函数会复制插件引用、内容变更回调函数和堆叠功能设置，
     * 所有引用都是浅拷贝
     *
     * @param other 需要拷贝的PlaceablePuzzleImpl实例
     */
    public PlaceablePuzzleImpl(@NotNull PlaceablePuzzleImpl other) {
        super(other);
        this.onContentsChanged = other.onContentsChanged;
        this.stackingEnabled = other.stackingEnabled;
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
        for (int slot : slots) {
            inventory.setItem(slot, null);
        }
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
            PaperScheduler.INSTANCE.entity(player).runDelayed(() -> onContentsChanged.accept(player), 1L);
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


    /**
     * 当GUI关闭时执行清理操作，将放置在拼图槽位中的物品归还给玩家
     * <p>
     * 如果玩家背包已满，则将物品掉落在玩家位置
     *
     * @param player    关闭GUI的玩家
     * @param inventory 被关闭的GUI的Inventory实例
     */
    @Override
    public void cleanupOnClose(Player player, Inventory inventory) {
        List<ItemStack> itemsToReturn = new ArrayList<>();
        for (int slot : this.getSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.isEmpty()) {
                itemsToReturn.add(item);
                inventory.setItem(slot, null);
            }
        }
        returnItemsToPlayer(player, itemsToReturn);
    }

    /**
     * 尝试接受物品到拼图中
     * <p>
     * 如果启用堆叠功能，会先尝试将物品堆叠到已有相同物品上，
     * 然后将剩余物品放置到空槽位中
     *
     * @param itemToAccept 要接受的物品
     * @param inventory    GUI库存实例
     * @return 如果成功接受物品返回true，否则返回false
     */
    @Override
    public boolean tryAcceptItem(ItemStack itemToAccept, Inventory inventory) {
        if (itemToAccept == null || itemToAccept.getAmount() <= 0) {
            return false;
        }
        int initialAmount = itemToAccept.getAmount();

        // 如果启用堆叠功能，先尝试堆叠到相同物品上
        if (this.stackingEnabled) {
            for (int slot : this.slots) {
                ItemStack existingItem = inventory.getItem(slot);
                if (existingItem != null && existingItem.isSimilar(itemToAccept)) {
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

        // 将剩余物品放置到空槽位中
        if (itemToAccept.getAmount() > 0) {
            for (int slot : this.slots) {
                ItemStack slotItem = inventory.getItem(slot);
                if (slotItem == null || slotItem.isEmpty()) {
                    inventory.setItem(slot, itemToAccept.clone());
                    itemToAccept.setAmount(0);
                    return true;
                }
            }
        }

        return itemToAccept.getAmount() < initialAmount;
    }

    /**
     * 获取变更回调函数
     *
     * @return 玩家变更回调函数的Consumer实例
     */
    @Override
    public Consumer<Player> getChangedCallBack() {
        return this.onContentsChanged;
    }

    /**
     * 检查是否有变更回调函数
     *
     * @return 如果有变更回调函数返回true，否则返回false
     */
    @Override
    public boolean hasChangedCallBack() {
        return this.onContentsChanged != null;
    }


}
