package cn.gloomcore.ui.puzzle;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 一个接口，用于标识那些在GUI关闭时需要执行清理操作的拼图。
 * 例如，退还玩家放置在输入槽中的物品。
 */
public interface PlaceablePuzzle {

    /**
     * 当GUI被关闭时执行的清理逻辑。
     *
     * @param player    关闭GUI的玩家
     * @param inventory 被关闭的GUI的Inventory实例
     */
    void cleanupOnClose(Player player, Inventory inventory);


    void tryAcceptItem(ItemStack itemToAccept, Inventory guiInventory);

}
