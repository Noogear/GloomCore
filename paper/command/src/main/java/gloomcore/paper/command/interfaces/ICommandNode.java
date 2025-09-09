package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 所有命令节点的根接口，定义了最基本的功能。
 */
public interface ICommandNode {
    /**
     * 获取命令节点的字面量名称。
     */
    String getName();

    /**
     * 构建并返回 Brigadier 的 ArgumentBuilder。
     * 这是将我们的面向对象节点转换为 Brigadier 节点的核心方法。
     */
    ArgumentBuilder<CommandSourceStack, ?> build();

    NodeType getNodeType();

    enum NodeType {
        LITERAL,
        ARGUMENT
    }
}
