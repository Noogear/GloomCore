package gloomcore.paper.command.bootstrap;

import gloomcore.paper.command.interfaces.ICommandNode;

import java.util.Collection;

/**
 * 提供根命令节点的构建入口，供生命周期事件中调用。
 */
@FunctionalInterface
public interface CommandRootsProvider {
    Collection<? extends ICommandNode> buildRoots();
}

