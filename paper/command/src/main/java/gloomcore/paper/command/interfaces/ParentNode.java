package gloomcore.paper.command.interfaces;

import java.util.Collection;

/**
 * 实现此接口的命令节点可以包含子节点。
 *
 */
@SuppressWarnings("unused")
public interface ParentNode extends CommandNode {
    /**
     * 添加一个子节点（忽略 null）。
     *
     * @param child 子节点
     */
    void addChild(CommandNode child);

    /**
     * 返回所有子节点集合（可能为空集，不为 null）。
     *
     * @return 子节点集合
     */
    Collection<CommandNode> getChildren();
}
