package gloomcore.paper.command.framework.argument;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

/**
 * 代表一个通过“转换”原生Minecraft参数类型来工作的自定义参数节点。
 * 这是创建自定义参数最常用、最简单的方式。
 *
 * @param <T> 转换后最终生成的自定义类型 (e.g., GameMode)
 * @param <N> 被转换的原生 Minecraft 类型 (e.g., a String from StringArgumentType)
 */
public abstract class ConvertedArgumentNode<T, N> extends CustomArgumentNode<T, N> implements CustomArgumentType.Converted<@NotNull T, @NotNull N> {
    // 这个类是抽象的，它强制子类去实现 Converted 接口中的 convert() 和 getNativeType() 方法。
    // 用户不再需要关心 parse() 的实现细节。
}
