package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 代表一个纯粹的字面量命令节点，它本身不可执行，仅用于构建命令树结构。
 * 例如 /manage set [weather|motd] 中的 "set"
 */
public abstract class LiteralNode extends AbstractCommandNode {
    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> createBuilder() {
        return LiteralArgumentBuilder.literal(getName());
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LITERAL;
    }
}
