package cn.gloomcore.ui.puzzle;

import org.bukkit.entity.Player;

public interface PaginatedPuzzle extends Puzzle {

    boolean nextPage(Player player);

    boolean previousPage(Player player);

    boolean jumpToPage(int pageNumber, Player player);

    int getCurrentPage();

    int getTotalPages();

    @Override
    default PuzzleType getPuzzleType() {
        return PuzzleType.PAGINATED;
    }
}
