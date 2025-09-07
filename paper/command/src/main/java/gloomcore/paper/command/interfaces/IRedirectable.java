package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 实现此接口的命令节点支持 redirect 和 fork 功能。
 */
public interface IRedirectable {
    CommandNode<CommandSourceStack> getRedirectTarget();

    RedirectModifier<CommandSourceStack> getRedirectModifier();

    boolean isFork();
}
