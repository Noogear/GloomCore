package gloomcore.paper.gui.puzzle;

import gloomcore.paper.gui.context.Context;

public interface PaginatedPuzzle<C extends Context> extends Puzzle<C> {

    /**
     * 切换到下一页
     *
     * @param context 目标上下文
     * @return 如果成功切换到下一页返回true，否则返回false
     */
    boolean nextPage(C context);

    /**
     * 切换到上一页
     *
     * @param context 目标上下文
     * @return 如果成功切换到上一页返回true，否则返回false
     */
    boolean previousPage(C context);

    /**
     * 跳转到指定页码
     *
     * @param pageNumber 目标页码
     * @param context    目标上下文
     * @return 如果成功跳转到指定页返回true，否则返回false
     */
    boolean jumpToPage(int pageNumber, C context);

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    int getCurrentPage();

    /**
     * 获取总页数
     *
     * @return 总页数
     */
    int getTotalPages();

    @Override
    default PuzzleType getPuzzleType() {
        return PuzzleType.PAGINATED;
    }
}
