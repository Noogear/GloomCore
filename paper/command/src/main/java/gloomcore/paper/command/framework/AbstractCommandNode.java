package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import gloomcore.paper.command.interfaces.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * 抽象命令节点基类，实现核心接口并提供统一 build 逻辑。
 */
public abstract class AbstractCommandNode implements CommandNode, ParentNode, RequireableNode {

    private final List<CommandNode> children = new ArrayList<>();

    @Override
    public void addChild(CommandNode child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    @Override
    public Collection<CommandNode> getChildren() {
        return this.children;
    }

    /**
     * 创建具体节点的 Brigadier Builder 由子类实现。
     *
     * @return builder
     */
    protected abstract ArgumentBuilder<CommandSourceStack, ?> createBuilder();

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build() {
        ArgumentBuilder<CommandSourceStack, ?> builder = createBuilder();

        Predicate<CommandSourceStack> requirement = getRequirement();
        boolean hasCustomRequirement = requirement != null && requirement != RequireableNode.ALWAYS_TRUE;

        if (this instanceof PermissionNode p) {
            String perm = p.getPermission();
            if (perm != null && !perm.isBlank()) {
                Predicate<CommandSourceStack> permPredicate = src -> src.getSender().hasPermission(perm);
                requirement = hasCustomRequirement ? requirement.and(permPredicate) : permPredicate;
                hasCustomRequirement = true;
            }
        }
        if (hasCustomRequirement && requirement != RequireableNode.ALWAYS_TRUE) {
            builder.requires(requirement);
        }

        if (this instanceof ExecutableNode exec) {
            builder.executes(exec::execute);
        }

        if (this instanceof RedirectableNode redirectable) {
            com.mojang.brigadier.tree.CommandNode<CommandSourceStack> target = redirectable.getRedirectTarget();
            if (target != null) {
                if (!children.isEmpty() || this instanceof ExecutableNode) {
                    throw new IllegalStateException(
                            "Redirected node '" + getName() + "' cannot have children or an executor.");
                }
                builder.forward(target, redirectable.getRedirectModifier(), redirectable.isFork());
                return builder;
            }
        }

        for (CommandNode child : children) {
            builder.then(child.build());
        }
        return builder;
    }
}
