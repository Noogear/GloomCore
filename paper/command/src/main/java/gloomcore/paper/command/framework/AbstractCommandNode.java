package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import gloomcore.paper.command.interfaces.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 框架的核心抽象基类.
 * 实现了基本接口，并提供了一个智能的 build 方法，可根据节点实现的接口来组装功能。
 */
public abstract class AbstractCommandNode implements ICommandNode, IParentNode, IRequireable {

    private final List<ICommandNode> children = new ArrayList<>();

    @Override
    public Predicate<CommandSourceStack> getRequirement() {
        return source -> true;
    }

    @Override
    public void addChild(ICommandNode child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    @Override
    public Collection<ICommandNode> getChildren() {
        return this.children;
    }

    /**
     * 模板方法，由子类（LiteralNode, ArgumentNode）实现，用于创建特定类型的 Builder。
     *
     * @return a new ArgumentBuilder instance.
     */
    protected abstract ArgumentBuilder<CommandSourceStack, ?> createBuilder();


    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build() {
        ArgumentBuilder<CommandSourceStack, ?> builder = createBuilder();

        // 1. 应用权限
        builder.requires(getRequirement());

        // 2. 应用执行逻辑 (如果可执行)
        if (this instanceof IExecutable) {
            builder.executes(((IExecutable) this)::execute);
        }

        // 3. 应用重定向逻辑 (如果可重定向)
        if (this instanceof IRedirectable redirectable) {
            com.mojang.brigadier.tree.CommandNode<CommandSourceStack> target = redirectable.getRedirectTarget();

            if (target != null) {
                if (!children.isEmpty() || this instanceof IExecutable) {
                    throw new IllegalStateException("Redirected node '" + getName() + "' cannot have children or an executor.");
                }
                builder.forward(target, redirectable.getRedirectModifier(), redirectable.isFork());
                return builder;
            }
        }

        // 5. 构建并添加子节点
        for (ICommandNode child : getChildren()) {
            builder.then(child.build());
        }

        return builder;
    }
}
