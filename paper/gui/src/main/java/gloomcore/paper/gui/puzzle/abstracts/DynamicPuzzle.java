package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.paper.gui.context.Context;
import gloomcore.paper.gui.view.AbstractGui;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * 动态拼图抽象类，实现可更新的拼图组件
 * <p>
 * 动态/可变拼图在构造时必须与其所属 GUI 绑定��
 */
public abstract class DynamicPuzzle<C extends Context> extends AbstractPuzzle<C> {

    protected final AbstractGui<C> gui;

    protected DynamicPuzzle(Collection<Integer> slotList, @NotNull AbstractGui<C> gui) {
        super(slotList);
        this.gui = gui;
    }

    /**
     * 拷贝构造函数（保持原 GUI 绑定）
     */
    protected DynamicPuzzle(DynamicPuzzle<C> other) {
        super(other);
        this.gui = other.gui;
    }

    /**
     * 拷贝构造函数（可选择重新绑定到新的 GUI）
     */
    protected DynamicPuzzle(DynamicPuzzle<C> other, @NotNull AbstractGui<C> gui) {
        super(other);
        this.gui = gui;
    }

    /**
     * 更新拼图显示内容：始终使用绑定的 GUI ��行重渲染。
     *
     * @param context 需要更新拼图显示的上下文
     */
    @Override
    public void update(C context) {
        render(gui.getOwner(), gui.getInventory());
    }
}
