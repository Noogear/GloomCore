package gloomcore.paper.gui.puzzle;

import gloomcore.paper.gui.context.Context;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

/**
 * 一个接口，用于标识那些在GUI关闭时需要执行清理操作的拼图。
 * 例如，退还玩家放置在输入槽中的物品。
 */
public interface PlaceablePuzzle<C extends Context> extends Puzzle<C> {

    /**
     * 当GUI被关闭时执行的清理逻辑。
     */
    void cleanupOnClose();

    /**
     * 尝试接受物品到拼图中
     *
     * @param itemToAccept 要接受的物品
     * @param guiInventory GUI库存实例
     * @return 如果成功接受物品返回true，否则返回false
     */
    boolean tryAcceptItem(ItemStack itemToAccept, Inventory guiInventory);

    /**
     * 获取变更回调函数
     *
     * @return 上下文变更回调函数的Consumer实例
     */
    Consumer<C> getChangedCallBack();

    /**
     * 检查是否有变更回调函数
     *
     * @return 如果有变更回调函数返回true，否则返回false
     */
    boolean hasChangedCallBack();

    @Override
    default PuzzleType getPuzzleType() {
        return PuzzleType.PLACEABLE;
    }

    /**
     * 将物品归还给上下文的玩家
     *
     * @param context       上下文
     * @param itemsToReturn 需要归还的物品列表
     */
    default void returnItemsToPlayer(C context, List<ItemStack> itemsToReturn) {
        Player player = context.player();
        if (player == null || itemsToReturn.isEmpty()) return;
        Location location = player.getLocation();
        World world = location.getWorld();
        Inventory playerInventory = player.getInventory();
        for (ItemStack item : itemsToReturn) {
            if (!playerInventory.addItem(item).isEmpty() && world != null) {
                world.dropItem(location, item);
            }
        }
    }
}
