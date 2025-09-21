package gloomcore.paper.gui.puzzle;

import gloomcore.paper.contract.PlayerContext;

public interface PaginatedPuzzle<C extends PlayerContext> extends Puzzle<C> {

    /**
     * 切换到下一页。
     *
     * @return 如果成功切换到下一页返回true，否则返回false
     */
    boolean nextPage();

    /**
     * 切换到上一页。
     *
     * @return 如果成功切换到上一页返回true，否则返回false
     */
    boolean previousPage();

    /**
     * 跳转到指定页码。
     *
     * @param pageNumber 目标页码（1-based）
     * @return 如果成功跳转到指定页返回true，否则返回false
     */
    boolean jumpToPage(int pageNumber);

    /**
     * 获取当前页码。
     *
     * @return 当前页码（1-based）
     */
    int getCurrentPage();

    /**
     * 获取总页数。
     *
     * @return 总页数
     */
    int getTotalPages();

    @Override
    default PuzzleType getPuzzleType() {
        return PuzzleType.PAGINATED;
    }
}
