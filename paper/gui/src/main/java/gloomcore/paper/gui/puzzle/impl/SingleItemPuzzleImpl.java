package gloomcore.paper.gui.puzzle.impl;

import gloomcore.paper.gui.icon.IconDisplay;
import gloomcore.paper.gui.puzzle.abstracts.StaticPuzzle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 静态物品拼图实现类
 * <p>
 * 该类表示一个静态物品拼图，用于在GUI中显示不可交互的静态图标
 * 图标可以在多个槽位中显示，但不处理用户的点击事件
 */
public class SingleItemPuzzleImpl extends StaticPuzzle {
    private final IconDisplay display;

    /**
     * 构造函数，创建一个静态物品拼图实例
     *
     * @param slotList 拼图占据的槽位列表
     * @param display  需要显示的图标内容
     */
    public SingleItemPuzzleImpl(Collection<Integer> slotList, IconDisplay display) {
        super(slotList);
        this.display = display;
    }

    /**
     * 拷贝构造函数，基于另一个StaticItemsPuzzleImpl实例创建新实例
     * <p>
     * 该构造函数会复制槽位信息和图标显示内容
     *
     * @param other 需要拷贝的StaticItemsPuzzleImpl实例
     */
    public SingleItemPuzzleImpl(@NotNull SingleItemPuzzleImpl other) {
        super(other);
        this.display = other.display;
    }

    @Override
    public void render(Player player, @NotNull Inventory inventory) {
        ItemStack itemStack = display.parse(player);
        for (int slot : slots) {
            inventory.setItem(slot, itemStack);
        }
    }

    @Override
    public PuzzleType getPuzzleType() {
        return PuzzleType.COMMON;
    }
}
