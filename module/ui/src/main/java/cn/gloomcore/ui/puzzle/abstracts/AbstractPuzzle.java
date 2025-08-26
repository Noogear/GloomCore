package cn.gloomcore.ui.puzzle.abstracts;

import cn.gloomcore.ui.puzzle.Puzzle;

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

    @Override
    public int[] getSlots() {
        return slots;
    }

}
