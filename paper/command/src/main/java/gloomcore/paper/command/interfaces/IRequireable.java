package gloomcore.paper.command.interfaces;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.function.Predicate;

/**
 * 实现此接口的命令节点可以定义执行要求（例如权限）。
 */
public interface IRequireable {
    /**
     * 恒为 true 的公共常量，用于判定是否需要在 Brigadier 中注册 requires()。
     */
    Predicate<CommandSourceStack> ALWAYS_TRUE = source -> true;

    /**
     * 返回执行此节点所需的条件；默认总是允许。
     * 返回 null 也视为无额外限制。
     */
    default Predicate<CommandSourceStack> getRequirement() {
        return ALWAYS_TRUE;
    }
}
