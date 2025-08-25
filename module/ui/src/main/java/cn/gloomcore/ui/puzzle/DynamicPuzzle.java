package cn.gloomcore.ui.puzzle;

import cn.gloomcore.ui.PuzzleGuiView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * ��̬ƴͼ�����࣬ʵ�ֿɸ��µ�ƴͼ���
 * <p>
 * �����ṩ��һ�����·�������ƴͼ��Ҫˢ����ʾ����ʱ���á�
 * ��̬ƴͼ������Ӧ��Ϸ״̬�仯��ʵʱ��������GUI�е���ʾ
 */
public abstract class DynamicPuzzle implements Puzzle {
    /**
     * ����ƴͼ��ʾ����
     * <p>
     * �÷���������ҵ�ǰ�򿪵Ŀ���Ƿ�ΪPuzzleGuiView���ͣ�
     * �������������Ⱦƴͼ���ݵ��ÿ����
     *
     * @param player ��Ҫ����ƴͼ��ʾ�����
     */
    @Override
    public void update(Player player) {
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (topInventory.getHolder(false) instanceof PuzzleGuiView) {
            render(player, topInventory);
        }
    }


}
