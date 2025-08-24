package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.puzzle.Puzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class PlaceableSlotsPuzzle implements Puzzle {

    private final JavaPlugin plugin;
    private final List<Integer> slots;
    private final Consumer<Player> onContentsChanged;

    /**
     * ����һ���µĿɷ��ò�λƴͼʵ��
     *
     * @param plugin            ���ʵ�������ڵ�������
     * @param slots             �ɷ�����Ʒ�Ĳ�λ�б�
     * @param onContentsChanged ����λ���ݷ����仯ʱ�Ļص�����������Ϊnull
     */
    public PlaceableSlotsPuzzle(@NotNull JavaPlugin plugin, @NotNull List<Integer> slots, @Nullable Consumer<Player> onContentsChanged) {
        this.plugin = plugin;
        this.slots = slots;
        this.onContentsChanged = onContentsChanged;
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
        // This puzzle itself is static
    }
}
