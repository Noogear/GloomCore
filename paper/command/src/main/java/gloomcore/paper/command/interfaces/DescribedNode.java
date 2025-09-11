package gloomcore.paper.command.interfaces;

/**
 * 为命令节点提供简短的描述（用途/作用）。
 */
@FunctionalInterface
public interface DescribedNode {
    /**
     * 返回命令描述，null/空白视为无描述。
     *
     * @return 描述或 null/空串
     */
    String getDescription();
}
