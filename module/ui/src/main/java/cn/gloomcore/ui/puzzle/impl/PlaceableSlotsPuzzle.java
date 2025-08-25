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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * �ɷ��ò�λƴͼ�࣬�ṩһ��ɹ���ҷ�����Ʒ�Ŀղ�λ
 * <p>
 * ��ƴͼ��ָ����λ�д�����λ��������ҽ���Ʒ��������Щ��λ�У�
 * ���ܼ������ݱ仯�¼�����Ҫ����������Ҫ��ҷ�����Ʒ�Ľ��棬
 * ��ϳɽ��桢��Ʒѡ������
 */
public class PlaceableSlotsPuzzle implements Puzzle, PlaceablePuzzle {
    private final JavaPlugin plugin;
    private final List<Integer> slots;
    private final Consumer<Player> onContentsChanged;
    private final boolean stackingEnabled;


    public PlaceableSlotsPuzzle(@NotNull JavaPlugin plugin, @NotNull List<Integer> slots, @Nullable Consumer<Player> onContentsChanged, boolean stackingEnabled) {
        this.plugin = plugin;
        this.slots = slots;
        this.onContentsChanged = onContentsChanged;
        this.stackingEnabled = stackingEnabled;
    }

    /**
     * ��ȡƴͼռ�ݵ����в�λ
     *
     * @return �������в�λ�����ļ���
     */
    @Override
    public Collection<Integer> getSlots() {
        return slots;
    }

    /**
     * ��Ⱦƴͼ���ݵ�ָ�������
     * <p>
     * ������ָ����λ��գ�ʹ���Ϊ�ղ�λ����ҷ�����Ʒ
     *
     * @param player    Ŀ�����
     * @param inventory Ŀ����
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        slots.forEach(slot -> inventory.setItem(slot, null));
    }

    /**
     * �����λ����¼�
     * <p>
     * ������ҷ��û�ȡ����Ʒ���������ݱ仯�󴥷��ص�����
     *
     * @param event ������¼�
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(false);
        if (onContentsChanged != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    onContentsChanged.accept((Player) event.getWhoClicked());
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * ����ƴͼ��ʾ����
     * <p>
     * ��ƴͼΪ��̬���ͣ�����Ҫ���²���
     *
     * @param player Ŀ�����
     */
    @Override
    public void update(Player player) {
    }

    @Override
    public void cleanupOnClose(Player player, Inventory inventory) {
        List<ItemStack> itemsToReturn = new ArrayList<>();

        // 1. ��GUI��Inventory���ռ�������Ҫ�˻�����Ʒ
        for (int slot : this.getSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                itemsToReturn.add(item);
                // 2. ���GUI�еĲ�λ
                inventory.setItem(slot, null);
            }
        }

        if (itemsToReturn.isEmpty()) {
            return; // û����Ʒ��Ҫ�˻�
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
    public void tryAcceptItem(ItemStack itemToAccept, Inventory inventory) {
        if (this.stackingEnabled) {
            for (int slot : this.slots) {
                if (itemToAccept.getAmount() <= 0) return;

                ItemStack existingItem = inventory.getItem(slot);
                if (existingItem != null && existingItem.isSimilar(itemToAccept)) {
                    int space = existingItem.getMaxStackSize() - existingItem.getAmount();
                    if (space > 0) {
                        int amountToMove = Math.min(space, itemToAccept.getAmount());
                        existingItem.setAmount(existingItem.getAmount() + amountToMove);
                        itemToAccept.setAmount(itemToAccept.getAmount() - amountToMove);
                    }
                }
            }
        }

        for (int slot : this.slots) {
            if (itemToAccept.getAmount() <= 0) return;

            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, itemToAccept.clone());
                itemToAccept.setAmount(0);
                return;
            }
        }

    }


}
