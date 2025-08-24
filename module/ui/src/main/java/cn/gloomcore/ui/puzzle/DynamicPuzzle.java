package cn.gloomcore.ui.puzzle;

import cn.gloomcore.ui.PuzzleGuiView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * 动态拼图抽象类，实现可更新的拼图组件
 * <p>
 * 该类提供了一个更新方法，当拼图需要刷新显示内容时调用。
 * 动态拼图可以响应游戏状态变化并实时更新其在GUI中的显示
 */
public abstract class DynamicPuzzle implements Puzzle {
    /**
     * 更新拼图显示内容
     * <p>
     * 该方法会检查玩家当前打开的库存是否为PuzzleGuiView类型，
     * 如果是则重新渲染拼图内容到该库存中
     *
     * @param player 需要更新拼图显示的玩家
     */
    @Override
    public void update(Player player) {
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        if (topInventory.getHolder(false) instanceof PuzzleGuiView) {
            render(player, topInventory);
        }
    }

}
