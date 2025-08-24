package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.IconAction;
import cn.gloomcore.ui.icon.IconDisplay;
import cn.gloomcore.ui.puzzle.DynamicPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;


/**
 * ��ťƴͼ�࣬ʵ����һ���ɵ���İ�ť���
 * <p>
 * ��ťƴͼ�Ƕ�̬ƴͼ��һ�֣�ռ��һ���ض���λ����ʾһ��ͼ�꣬
 * ����ҵ��ʱ�ᴥ����Ӧ�Ķ�������ť����ۿ����Ǿ�̬�Ļ�������״̬��̬���ɵ�
 */
public class ButtonPuzzle extends DynamicPuzzle {
    private final int slot;
    private final Function<Player, IconDisplay> appearanceFunction;
    private final IconAction action;

    /**
     * ����һ���µİ�ťƴͼʵ��
     *
     * @param slot               ��ť���ڵĲ�λ
     * @param appearanceFunction �������ɰ�ť��۵ĺ������������״̬����ͼ����ʾ����
     * @param action             ��ť�����ʱִ�еĶ���
     */
    public ButtonPuzzle(int slot, @NotNull Function<Player, IconDisplay> appearanceFunction, @NotNull IconAction action) {
        this.slot = slot;
        this.appearanceFunction = appearanceFunction;
        this.action = action;
    }

    /**
     * ����һ���µľ�̬��ťƴͼʵ��
     *
     * @param slot       ��ť���ڵĲ�λ
     * @param staticItem ��̬ͼ����ʾ����
     * @param action     ��ť�����ʱִ�еĶ���
     */
    public ButtonPuzzle(int slot, @NotNull IconDisplay staticItem, @NotNull IconAction action) {
        this(slot, player -> staticItem, action);
    }

    /**
     * ��ȡ��ťƴͼռ�ݵĲ�λ
     *
     * @return ����������λ�����ļ���
     */
    @Override
    public Set<Integer> getSlots() {
        return Set.of(slot);
    }


    /**
     * ��Ⱦ��ťƴͼ��ָ�������
     * <p>
     * �������״̬����ͼ����ʾ���ݲ����õ�ָ����λ
     *
     * @param player    Ŀ�����
     * @param inventory Ŀ����
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        inventory.setItem(slot, appearanceFunction.apply(player).parse(player));
    }


    /**
     * ����ť����¼�
     * <p>
     * ������¼�ת����ͼ�궯�����д���
     *
     * @param event ������¼�
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        action.onClick(event);
    }

}
