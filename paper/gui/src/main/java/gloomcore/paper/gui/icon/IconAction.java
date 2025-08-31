package gloomcore.paper.gui.icon;

import gloomcore.paper.util.Log;
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
public interface IconAction {

    /**
     * 创建一个ClickAction实例，当点击类型与指定类型匹配时执行相应操作
     *
     * @param clickMap 指定的点击类型与操作的映射
     * @return ClickAction实例
     */
    static @NotNull IconAction of(@NotNull EnumMap<ClickType, PlayerAction> clickMap) {
        return (type, player) -> {
            if (clickMap.containsKey(type)) {
                clickMap.get(type).launch(player, Log.INSTANCE::error);
            }
        };
    }

    /**
     * 创建一个ClickAction实例，当点击类型与指定类型匹配时执行相应操作
     *
     * @param clickType    指定的点击类型
     * @param playerAction 需要执行的操作
     * @return ClickAction实例
     */
    static @NotNull IconAction of(@NotNull ClickType clickType, @NotNull PlayerAction playerAction) {
        return (type, player) -> {
            if (type == clickType) {
                playerAction.launch(player, Log.INSTANCE::error);
            }
        };
    }

    /**
     * 根据点击类型执行相应操作
     *
     * @param type   点击类型
     * @param player 执行操作的玩家，可以为null
     */
    void onClick(@NotNull ClickType type, @Nullable Player player);

    /**
     * 处理点击事件
     *
     * @param event 点击事件
     */
    default void onClick(@NotNull InventoryClickEvent event) {
        onClick(event.getClick(), (Player) event.getWhoClicked());
    }


}
