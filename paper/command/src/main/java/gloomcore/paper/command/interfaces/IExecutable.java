package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 实现此接口的命令节点是可执行的。
 */
@FunctionalInterface
public interface IExecutable {
    int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
}
