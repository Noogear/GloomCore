package gloomcore.paper.command.interfaces;

import java.util.Collection;

/**
 * 实现此接口的命令节点可以包含子节点。
 */
public interface IParentNode extends ICommandNode {
    void addChild(ICommandNode child);

    Collection<ICommandNode> getChildren();
}
