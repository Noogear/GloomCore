package gloomcore.paper.command.interfaces;

import java.util.Collection;

/**
 * 实现此接口的命令节点可以包含子节点。
 */
public interface IParentNode extends ICommandNode {
    /**
     * 添加一个子节点（忽略 null）。
     *
     * @param child 子节点
     */
    void addChild(ICommandNode child);

    /**
     * 返回所有子节点集合（可能为空集合，不为 null）。
     *
     * @return 子节点集合
     */
    Collection<ICommandNode> getChildren();
}
