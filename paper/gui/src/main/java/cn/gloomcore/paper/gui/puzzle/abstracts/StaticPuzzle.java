package cn.gloomcore.paper.gui.puzzle.abstracts;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

/**
 * 静态拼图抽象类，实现不可交互的拼图组件
 * <p>
 * 静态拼图通常用于显示静态内容，不响应点击事件或更新操作。
 * 该类默认取消所有点击事件并不执行任何更新操作
 */
public abstract class StaticPuzzle extends AbstractPuzzle {

    protected StaticPuzzle(Collection<Integer> slotList) {
        super(slotList);
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 StaticPuzzle 对象
     */
    protected StaticPuzzle(StaticPuzzle other) {
        super(other);
    }

    /**
     * 处理库存点击事件，默认取消所有点击操作
     * <p>
     * 静态拼图不响应点击事件，因此默认实现是取消所有点击事件
     *
     * @param event 库存点击事件
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    /**
     * 更新拼图显示内容，静态拼图不执行任何操作
     * <p>
     * 静态拼图的内容不会发生变化，因此此方法为空实现
     *
     * @param player 目标玩家
     */
    @Override
    public void update(Player player) {
    }


}
