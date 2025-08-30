package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.icon.Icon;
import gloomcore.paper.gui.puzzle.abstracts.DynamicPuzzle;
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
public class ButtonPuzzleImpl extends DynamicPuzzle {
    private final Icon icon;

    /**
     * 构造函数，创建一个按钮拼图实例
     *
     * @param slotList 拼图占据的槽位列表
     * @param icon     需要显示的按钮图标
     */
    public ButtonPuzzleImpl(Collection<Integer> slotList, Icon icon) {
        super(slotList);
        this.icon = icon;
    }

    /**
     * 拷贝构造函数，基于另一个ButtonPuzzleImpl实例创建新实例
     *
     * @param other 需要拷贝的ButtonPuzzleImpl实例
     */
    public ButtonPuzzleImpl(ButtonPuzzleImpl other) {
        super(other);
        this.icon = new Icon(other.icon);
    }


    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        for (int slot : slots) {
            inventory.setItem(slot, icon.display(player));
        }
    }


    @Override
    public void onClick(InventoryClickEvent event) {
        icon.onClick(event);
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }

}
