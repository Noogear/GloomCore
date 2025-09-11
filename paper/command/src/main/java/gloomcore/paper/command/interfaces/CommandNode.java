package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 所有命令节点的根接口。定义名称、构建 Brigadier Builder、节点类型。
 */
public interface CommandNode {
    /**
     * 获取命令节点的字面量名称。
     *
     * @return 名称（非空）
     */
    String getName();

    /**
     * 构建并返回 Brigadier 的 ArgumentBuilder。
     * 这是将面向对象节点转换为 Brigadier 节点的核心方法。
     *
     * @return 已配置的 ArgumentBuilder
     */
    ArgumentBuilder<CommandSourceStack, ?> build();

    /**
     * 返回当前节点类型（字面量或参数）。
     *
     * @return 节点类型
     */
    NodeType getNodeType();

    /**
     * 命令节点类型枚举。
     */
    enum NodeType {
        /**
         * 字面量节点
         */
        LITERAL,
        /**
         * 参数节点
         */
        ARGUMENT
    }
}
