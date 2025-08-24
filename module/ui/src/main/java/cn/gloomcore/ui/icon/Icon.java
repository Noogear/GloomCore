package cn.gloomcore.ui.icon;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ͼ���࣬���ڱ�ʾUI�е�ͼ��Ԫ��
 * <p>
 * ����ͼ�����ʾ���ݺ͵��ʱ����Ӧ����
 */
public class Icon {
    protected IconDisplay display;
    protected IconAction iconAction;

    /**
     * ���캯��������һ��ֻ����ʾ���ݵ�ͼ��
     *
     * @param display ͼ�����ʾ����
     */
    public Icon(@NotNull IconDisplay display) {
        this.display = display;
    }

    /**
     * ���캯��������һ��������ʾ���ݺ͵��������ͼ��
     *
     * @param display    ͼ�����ʾ����
     * @param iconAction ͼ��ĵ������
     */
    public Icon(@NotNull IconDisplay display, @Nullable IconAction iconAction) {
        this.display = display;
        this.iconAction = iconAction;
    }

    /**
     * ����ͼ��ĵ���¼�
     *
     * @param event ����¼�
     * @return ��ǰͼ��ʵ����֧����ʽ����
     */
    public Icon onClick(InventoryClickEvent event) {
        if (iconAction != null) {
            iconAction.onClick(event);
        }
        return this;
    }

    /**
     * ����ָ��������͵ĵ���¼�
     *
     * @param clickType �������
     * @param player    ��������
     * @return ��ǰͼ��ʵ����֧����ʽ����
     */
    public Icon onClick(@NotNull ClickType clickType, @Nullable Player player) {
        if (iconAction != null) {
            iconAction.onClick(clickType, player);
        }
        return this;
    }

    /**
     * ����ͼ��ĵ������
     *
     * @param iconAction �������
     * @return ��ǰͼ��ʵ����֧����ʽ����
     */
    public Icon setClickAction(@Nullable IconAction iconAction) {
        this.iconAction = iconAction;
        return this;
    }

    /**
     * ����ͼ�����ʾ����
     *
     * @param display ��ʾ����
     * @return ��ǰͼ��ʵ����֧����ʽ����
     */
    public Icon setDisplay(@NotNull IconDisplay display) {
        this.display = display;
        return this;
    }

    /**
     * ��ȡͼ�����ʾ��Ʒ
     *
     * @return ͼ�����Ʒ��ʵ��
     */
    public ItemStack display() {
        return display.parse();
    }

}
