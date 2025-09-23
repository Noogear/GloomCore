package gloomcore.paper.gui.icon;

import gloomcore.paper.contract.Action;
import gloomcore.paper.contract.Context;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

/**
 * 点击事件接口，用于处理用户UI界面的点击事件
 */
@FunctionalInterface
public interface IconAction<C extends Context<Player>> {

    /**
     * 创建一个ClickAction实例，当点击类型与指定类型匹配时执行相应操作
     *
     * @param clickMap 指定的点击类型与操作的映射
     * @return ClickAction实例
     */
    static <C extends Context<Player>> @NotNull IconAction<C> of(@NotNull EnumMap<ClickType, Action<C, Void>> clickMap) {
        return (type, context) -> {
            if (clickMap.containsKey(type)) {
                clickMap.get(type).execute(context);
            }
        };
    }

    /**
     * 创建一个ClickAction实例，当点击类型与指定类型匹配时执行相应操作
     *
     * @param clickType     指定的点击类型
     * @param contextAction 需要执行的操作
     * @return ClickAction实例
     */
    static <C extends Context<Player>> @NotNull IconAction<C> of(@NotNull ClickType clickType, @NotNull Action<C, Void> contextAction) {
        return (type, context) -> {
            if (type == clickType) {
                contextAction.execute(context);
            }
        };
    }

    /**
     * 根据点击类型执行相应操作
     *
     * @param type    点击类型
     * @param context 执行操作的上下文，可以为null
     */
    void onClick(@NotNull ClickType type, @Nullable C context);

    /**
     * 处理点击事件
     *
     * @param event   点击事件
     * @param context 上下文
     */
    default void onClick(@NotNull InventoryClickEvent event, @Nullable C context) {
        onClick(event.getClick(), context);
    }
}
