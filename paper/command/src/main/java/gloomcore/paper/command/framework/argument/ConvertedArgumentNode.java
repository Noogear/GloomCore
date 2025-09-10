package gloomcore.paper.command.framework.argument;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

/**
 * 代表一个通过“转换”原生 Minecraft 参数类型来工作的自定义参数节点。
 * 这是创建自定义参数最常用、最简单的方式。
 *
 * @param <T> 转换后最终生成的自定义类型 (e.g., GameMode)
 * @param <N> 被转换的原生 Minecraft 类型 (e.g., String 原始值)
 */
public abstract class ConvertedArgumentNode<T, N>
        extends CustomArgumentNode<T, N>
        implements CustomArgumentType.Converted<@NotNull T, @NotNull N> {
    // 抽象类：子类需实现 convert() 与 getNativeType()，无需关心 parse() 细节。
}
