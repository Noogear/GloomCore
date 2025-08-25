package cn.gloomcore.ui.puzzle;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 静态拼图抽象类，实现不可交互的拼图组件
 * <p>
 * 静态拼图通常用于显示静态内容，不响应点击事件或更新操作。
 * 该类默认取消所有点击事件并不执行任何更新操作
 */
public abstract class StaticPuzzle implements Puzzle {

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
