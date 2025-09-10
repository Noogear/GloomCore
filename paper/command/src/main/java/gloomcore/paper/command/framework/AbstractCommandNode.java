package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import gloomcore.paper.command.interfaces.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 框架的核心抽象基类。
 * 实现了基本接口，并提供智能 build 方法。
 */
public abstract class AbstractCommandNode implements ICommandNode, IParentNode, IRequireable {

    private final List<ICommandNode> children = new ArrayList<>();

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
     * 创建具体节点 Builder（由子类实现）。
     *
     * @return builder
     */
    protected abstract ArgumentBuilder<CommandSourceStack, ?> createBuilder();

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build() {
        ArgumentBuilder<CommandSourceStack, ?> builder = createBuilder();

        Predicate<CommandSourceStack> requirement = getRequirement();
        boolean hasCustomRequirement = requirement != null && requirement != IRequireable.ALWAYS_TRUE;

        if (this instanceof IPermission p) {
            String perm = p.getPermission();
            if (perm != null && !perm.isBlank()) {
                Predicate<CommandSourceStack> permPredicate = src -> src.getSender().hasPermission(perm);
                requirement = hasCustomRequirement ? requirement.and(permPredicate) : permPredicate;
                hasCustomRequirement = true;
            }
        }
        if (hasCustomRequirement && requirement != null && requirement != IRequireable.ALWAYS_TRUE) {
            builder.requires(requirement);
        }

        if (this instanceof IExecutable exec) {
            builder.executes(exec::execute);
        }

        if (this instanceof IRedirectable redirectable) {
            com.mojang.brigadier.tree.CommandNode<CommandSourceStack> target = redirectable.getRedirectTarget();
            if (target != null) {
                if (!children.isEmpty() || this instanceof IExecutable) {
                    throw new IllegalStateException(
                            "Redirected node '" + getName() + "' cannot have children or an executor.");
                }
                builder.forward(target, redirectable.getRedirectModifier(), redirectable.isFork());
                return builder;
            }
        }

        for (ICommandNode child : children) {
            builder.then(child.build());
        }
        return builder;
    }
}
