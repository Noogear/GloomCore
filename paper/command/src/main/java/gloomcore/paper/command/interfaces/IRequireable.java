package gloomcore.paper.command.interfaces;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

/**
 * 实现此接口的命令节点可以定义执行要求（例如权限）。
 */
@FunctionalInterface
public interface IRequireable {
    Predicate<CommandSourceStack> getRequirement();
}
