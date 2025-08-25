package cn.gloomcore.ui.puzzle;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * ƴͼ�ӿڣ�������GUI�пɽ�������Ļ�����Ϊ
 * <p>
 * ƴͼ��GUI����Ļ�����ɵ�Ԫ��ÿ��ƴͼ����ռ��һ��������λ��
 * ���ܴ������¼�����Ⱦ���ݺ͸�����ʾ
 */
public interface Puzzle {

    /**
     * ��ȡƴͼռ�ݵ����в�λ
     *
     * @return �������в�λ�����ļ���
     */
    Collection<Integer> getSlots();

    /**
     * ��Ⱦƴͼ���ݵ�ָ�������
     *
     * @param player    Ŀ�����
     * @param inventory Ŀ����
     */
    void render(Player player, @NotNull Inventory inventory);

    /**
     * ���������¼�
     *
     * @param event ������¼�
     */
    void onClick(InventoryClickEvent event);

    /**
     * ����ƴͼ��ʾ����
     *
     * @param player Ŀ�����
     */
    void update(Player player);



}
