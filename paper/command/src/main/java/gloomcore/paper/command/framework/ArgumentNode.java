package gloomcore.paper.command.framework;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 代表一个参数命令节点。
 *
 * @param <T> 参数返回的中间结果类型 (e.g., PlayerArgument.Result)
 */
public abstract class ArgumentNode<T> extends AbstractCommandNode {

    /**
     * 获取此参数的 ArgumentType。
     *
     * @return The ArgumentType for this node.
     */
    public abstract ArgumentType<T> getType();

    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> createBuilder() {
        // 参数节点的核心：使用 RequiredArgumentBuilder
        return RequiredArgumentBuilder.argument(getName(), getType());
    }
}
