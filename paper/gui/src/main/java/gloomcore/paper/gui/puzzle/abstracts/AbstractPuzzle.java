package gloomcore.paper.gui.puzzle.abstracts;

import gloomcore.paper.gui.puzzle.Puzzle;

import java.util.Collection;

public abstract class AbstractPuzzle implements Puzzle {
    protected final int[] slots;

    protected AbstractPuzzle(Collection<Integer> slotList) {
        this.slots = slotList.stream()
                .distinct()
                .sorted()
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * 拷贝构造函数
     *
     * @param other 要拷贝的源 AbstractPuzzle 对象
     */
    protected AbstractPuzzle(AbstractPuzzle other) {
        this.slots = other.slots.clone();
    }

    @Override
    public int[] getSlots() {
        return slots;
    }

}
