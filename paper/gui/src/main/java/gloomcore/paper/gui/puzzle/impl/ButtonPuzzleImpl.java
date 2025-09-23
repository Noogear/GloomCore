package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.contract.Context;
import gloomcore.paper.gui.icon.Icon;
import gloomcore.paper.gui.puzzle.abstracts.DynamicPuzzle;
import gloomcore.paper.gui.view.AbstractGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 按钮拼图实现类
 * <p>
 * 该类表示一个按钮拼图，用于在GUI中显示一个可点击的按钮
 * 按钮可以在多个槽位中显示，并处理用户的点击事件
 */
public class ButtonPuzzleImpl<C extends Context<Player>> extends DynamicPuzzle<C> {
    private final Icon<C> icon;

    /**
     * 构造函数，创建一个按钮拼图实例（绑定GUI）
     *
     * @param slotList 拼图占据的槽位列表
     * @param icon     需要显示的按钮图标
     * @param gui      绑定的GUI
     */
    public ButtonPuzzleImpl(Collection<Integer> slotList, Icon<C> icon, AbstractGui<C> gui) {
        super(slotList, gui);
        this.icon = icon;
    }

    /**
     * 拷贝构造函数，基于另一个ButtonPuzzleImpl实例创建新实例
     *
     * @param other 需要拷贝的ButtonPuzzleImpl实例
     */
    public ButtonPuzzleImpl(ButtonPuzzleImpl<C> other) {
        super(other);
        this.icon = new Icon<>(other.icon);
    }

    /**
     * 带 GUI 的拷贝构造器：复制按钮拼图，并重新绑定到新的 GUI。
     */
    public ButtonPuzzleImpl(ButtonPuzzleImpl<C> other, AbstractGui<C> gui) {
        super(other, gui);
        this.icon = new Icon<>(other.icon);
    }

    @Override
    public void render(C context, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, icon.display(context));
        }
    }


    @Override
    public void onClick(InventoryClickEvent event, C owner) {
        icon.onClick(event, gui.getOwner());
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }

}
