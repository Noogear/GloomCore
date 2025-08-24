package cn.gloomcore.ui.icon;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * �����ӿڣ���ʾ����ִ�еĲ���
 * <p>
 * �ýӿ���һ������ʽ�ӿڣ�����ͨ��lambda���ʽ�򷽷�������ʵ��
 * �ṩ������ִ�з�ʽ������Ҳ����Ͳ���������ִ�з�ʽ
 */
@FunctionalInterface
public interface Action {

    /**
     * ִ�ж���
     *
     * @param player ִ�ж�������ң�����Ϊnull
     */
    void run(@Nullable Player player);

    /**
     * ִ�ж�������ָ�����
     * <p>
     * �˷�������� {@link #run(Player)} ������null��Ϊ����
     */
    default void run() {
        run(null);
    }
}
