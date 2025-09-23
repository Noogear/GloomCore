package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.paper.contract.Context;
import gloomcore.paper.gui.view.AbstractGui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 动态拼图抽象类，实现可更新的拼图组件。
 * <p>
 * 动态/可变拼图在构造时必须与其所属 GUI 绑定。
 */
public abstract class DynamicPuzzle<C extends Context<Player>> extends AbstractPuzzle<C> {

    protected final AbstractGui<C> gui;

    protected DynamicPuzzle(Collection<Integer> slotList, @NotNull AbstractGui<C> gui) {
        super(slotList);
        this.gui = gui;
    }

    /**
     * 拷贝构造函数（保持原 GUI 绑定）。
     */
    protected DynamicPuzzle(DynamicPuzzle<C> other) {
        super(other);
        this.gui = other.gui;
    }

    /**
     * 拷贝构造函数（可选择重新绑定到新的 GUI）。
     */
    protected DynamicPuzzle(DynamicPuzzle<C> other, @NotNull AbstractGui<C> gui) {
        super(other);
        this.gui = gui;
    }

    /**
     * 无参更新：始终使用绑定的 GUI 进行重渲染。
     */
    public void update() {
        render(gui.getOwner(), gui.getInventory());
    }
}
