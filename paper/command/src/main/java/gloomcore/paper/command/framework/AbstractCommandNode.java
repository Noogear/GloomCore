package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import gloomcore.paper.command.interfaces.*;
import gloomcore.paper.command.util.CommandNodeUtils;
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

        // 统一合成 requires 谓词（权限 + 其它要求），避免重复逻辑
        Predicate<CommandSourceStack> requirement = CommandNodeUtils.effectiveRequirement(this);
        if (requirement != RequireableNode.ALWAYS_TRUE) {
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
