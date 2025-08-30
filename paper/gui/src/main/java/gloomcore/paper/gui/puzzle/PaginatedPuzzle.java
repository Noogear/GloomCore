package gloomcore.paper.gui.puzzle;

import org.bukkit.entity.Player;

public interface PaginatedPuzzle extends Puzzle {

    /**
     * 切换到下一页
     *
     * @param player 目标玩家
     * @return 如果成功切换到下一页返回true，否则返回false
     */
    boolean nextPage(Player player);

    /**
     * 切换到上一页
     *
     * @param player 目标玩家
     * @return 如果成功切换到上一页返回true，否则返回false
     */
    boolean previousPage(Player player);

    /**
     * 跳转到指定页码
     *
     * @param pageNumber 目标页码
     * @param player     目标玩家
     * @return 如果成功跳转到指定页返回true，否则返回false
     */
    boolean jumpToPage(int pageNumber, Player player);

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
