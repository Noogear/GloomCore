package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 实现此接口的命令节点是可执行的。
 */
@FunctionalInterface
public interface IExecutable {
    /**
     * 执行命令逻辑。
     *
     * @param context Brigadier 上下文（包含参数与源）
     * @return 返回一个整数（通常用作结果码）
     * @throws CommandSyntaxException 语法或权限等执行异常
     */
    int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
}
