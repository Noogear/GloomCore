package gloomcore.paper.command.framework.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import gloomcore.paper.command.framework.AbstractCommandNode;
import gloomcore.paper.command.interfaces.ISuggestable;
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
        RequiredArgumentBuilder<CommandSourceStack, T> builder = RequiredArgumentBuilder.argument(getName(), getType());

        // 检查当前实例是否实现了 ISuggestable 接口
        if (this instanceof ISuggestable) {
            // 如果是，就调用接口的方法来应用建议
            builder.suggests(((ISuggestable) this).getSuggestionsProvider());
        }

        return builder;
    }
}
