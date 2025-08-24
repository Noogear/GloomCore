package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.Icon;
import cn.gloomcore.ui.puzzle.DynamicPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ��ҳ��Ʒƴͼ�࣬������GUI����ʾ�ɷ�ҳ����Ʒ�б�
 * <p>
 * ��ƴͼ��һ����Ʒ��ҳ��ʾ��ָ���Ĳ�λ�У�֧�ַ�ҳ���ܣ�
 * ���ܴ�����Ʒ�ĵ���¼���ÿҳ��ʾ����Ʒ����ȡ�����ṩ�Ĳ�λ����
 */
public class PaginatedItemsPuzzle extends DynamicPuzzle {
    private final List<Integer> slots;
    private final List<Icon> allItems;
    private int currentPage = 0;

    /**
     * ����һ���µķ�ҳ��Ʒƴͼʵ��
     *
     * @param slots    ������ʾ��Ʒ�Ĳ�λ�б�
     * @param allItems ������Ҫ��ҳ��ʾ����Ʒͼ���б�
     */
    public PaginatedItemsPuzzle(List<Integer> slots, List<Icon> allItems) {
        this.slots = slots;
        this.allItems = allItems;
    }

    /**
     * ��ȡƴͼռ�ݵ����в�λ
     *
     * @return �������в�λ�������б�
     */
    @Override
    public List<Integer> getSlots() {
        return slots;
    }

    /**
     * ��Ⱦ��ǰҳ����Ʒ��ָ�������
     * <p>
     * ����������в�λ��Ȼ����㵱ǰҳӦ��ʾ����Ʒ��Χ��
     * ����Ӧ��Ʒ���õ���λ��
     *
     * @param player    Ŀ�����
     * @param inventory Ŀ����
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        // ������в�λ
        slots.forEach(slot -> inventory.setItem(slot, null));

        int itemsPerPage = slots.size();
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < allItems.size()) {
                inventory.setItem(slots.get(i), allItems.get(itemIndex).display());
            } else {
                break; // û�и�����Ʒ��
            }
        }
    }

    /**
     * �л�����һҳ
     * <p>
     * ���������һҳ�������ӵ�ǰҳ�벢������ʾ
     *
     * @param player Ŀ�����
     * @return ����ɹ��л�����һҳ����true�����򷵻�false
     */
    public boolean nextPage(Player player) {
        if ((currentPage + 1) * slots.size() < allItems.size()) {
            currentPage++;
            update(player);
            return true;
        }
        return false;
    }

    /**
     * �л�����һҳ
     * <p>
     * ���������һҳ������ٵ�ǰҳ�벢������ʾ
     *
     * @param player Ŀ�����
     * @return ����ɹ��л�����һҳ����true�����򷵻�false
     */
    public boolean previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            update(player);
            return true;
        }
        return false;
    }

    /**
     * ������Ʒ����¼�
     * <p>
     * ���ݵ���Ĳ�λȷ����Ӧ����Ʒ��Ȼ���¼�ת��������Ʒ����
     *
     * @param event ������¼�
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        int clickedRawSlot = event.getRawSlot();
        int slotIndexInPage = this.slots.indexOf(clickedRawSlot);
        if (slotIndexInPage == -1) {
            return;
        }
        int itemsPerPage = this.slots.size();
        int globalItemIndex = (currentPage * itemsPerPage) + slotIndexInPage;
        if (globalItemIndex < allItems.size()) {
            Icon clickedItem = allItems.get(globalItemIndex);
            clickedItem.onClick(event);
        }

    }

}
