package cn.gloomcore.ui.puzzle.impl;

import cn.gloomcore.ui.icon.Icon;
import cn.gloomcore.ui.puzzle.abstracts.DynamicPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


/**
 * 按钮拼图类，实现了一个可点击的按钮组件
 * <p>
 * 按钮拼图是动态拼图的一种，占据一个特定槽位并显示一个图标，
 * 当玩家点击时会触发相应的动作。按钮的外观可以是静态的或基于玩家状态动态生成的
 */
public class DynamicItemPuzzleImpl extends DynamicPuzzle {
    private final Icon icon;

    public DynamicItemPuzzleImpl(Collection<Integer> slotList, Icon icon) {
        super(slotList);
        this.icon = icon;
    }

    /**
     * 渲染按钮拼图到指定库存中
     * <p>
     * 根据玩家状态生成图标显示内容并设置到指定槽位
     *
     * @param player    目标玩家
     * @param inventory 目标库存
     */
    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, icon.display(player));
        }
    }

    /**
     * 处理按钮点击事件
     * <p>
     * 将点击事件转发给图标动作进行处理
     *
     * @param event 库存点击事件
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        icon.onClick(event);
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }

}
