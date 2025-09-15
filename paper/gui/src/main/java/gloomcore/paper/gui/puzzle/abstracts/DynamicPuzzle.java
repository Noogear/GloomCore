package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.paper.gui.view.AbstractGui;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * 动态拼图抽象类，实现可更新的拼图组件
 * <p>
 * 该类提供了一个更新方法，当拼图需要刷新显示内容时调用。
 * 动态拼图可以响应游戏状态变化并实时更新其在GUI中的显示
 */
public abstract class DynamicPuzzle extends AbstractPuzzle {

    protected DynamicPuzzle(Collection<Integer> slotList) {
        super(slotList);
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 DynamicPuzzle 对象
     */
    protected DynamicPuzzle(DynamicPuzzle other) {
        super(other);
    }

    /**
     * 更新拼图显示内容
     * <p>
     * 该方法会检查玩家当前打开的库存是否为AbstractGui类型，
     * 如果是则重新渲染拼图内容到该库存中
     *
     * @param player 需要更新拼图显示的玩家
     */
    @Override
    public void update(Player player) {
        if (player.getOpenInventory().getTopInventory().getHolder(false) instanceof AbstractGui gui) {
            render(gui.getOwner(), gui.getInventory());
        }
    }


}
