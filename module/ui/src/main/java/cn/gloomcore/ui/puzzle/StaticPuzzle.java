package cn.gloomcore.ui.puzzle;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * ��̬ƴͼ�����࣬ʵ�ֲ��ɽ�����ƴͼ���
 * <p>
 * ��̬ƴͼͨ��������ʾ��̬���ݣ�����Ӧ����¼�����²�����
 * ����Ĭ��ȡ�����е���¼�����ִ���κθ��²���
 */
public abstract class StaticPuzzle implements Puzzle {

    /**
     * ���������¼���Ĭ��ȡ�����е������
     * <p>
     * ��̬ƴͼ����Ӧ����¼������Ĭ��ʵ����ȡ�����е���¼�
     *
     * @param event ������¼�
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    /**
     * ����ƴͼ��ʾ���ݣ���̬ƴͼ��ִ���κβ���
     * <p>
     * ��̬ƴͼ�����ݲ��ᷢ���仯����˴˷���Ϊ��ʵ��
     *
     * @param player Ŀ�����
     */
    @Override
    public void update(Player player) {
    }

}
