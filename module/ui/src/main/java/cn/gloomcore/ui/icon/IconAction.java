package cn.gloomcore.ui.icon;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

/**
 * ����¼��ӿڣ����ڴ����û�UI����ĵ���¼�
 */
@FunctionalInterface
public interface IconAction {

    /**
     * ����һ��ClickActionʵ���������������ָ������ƥ��ʱִ����Ӧ����
     *
     * @param clickMap ָ���ĵ�������������ӳ��
     * @return ClickActionʵ��
     */
    static @NotNull IconAction of(@NotNull EnumMap<ClickType, Action> clickMap) {
        return (type, player) -> {
            if (clickMap.containsKey(type)) {
                clickMap.get(type).run(player);
            }
        };
    }

    /**
     * ����һ��ClickActionʵ���������������ָ������ƥ��ʱִ����Ӧ����
     *
     * @param clickType ָ���ĵ������
     * @param action    ��Ҫִ�еĲ���
     * @return ClickActionʵ��
     */
    static @NotNull IconAction of(@NotNull ClickType clickType, @NotNull Action action) {
        return (type, player) -> {
            if (type == clickType) {
                action.run(player);
            }
        };
    }

    /**
     * ���ݵ������ִ����Ӧ����
     *
     * @param type   �������
     * @param player ִ�в�������ң�����Ϊnull
     */
    void onClick(@NotNull ClickType type, @Nullable Player player);

    /**
     * �������¼�
     *
     * @param event ����¼�
     */
    default void onClick(@NotNull InventoryClickEvent event) {
        onClick(event.getClick(), (Player) event.getWhoClicked());
    }


}
