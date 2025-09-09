package gloomcore.paper.command.framework.argument;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import gloomcore.paper.command.framework.AbstractCommandNode;
import gloomcore.paper.command.interfaces.ISuggestable;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * 代表一个自定义参数节点，它通过手动解析输入字符串来工作。
 * 节点本身就是它自己的 ArgumentType。
 *
 * @param <T> 解析后最终生成的自定义类型 (e.g., Kit)
 * @param <N> 用于在客户端显示的等效原生 Minecraft 类型 (e.g., StringArgumentType)
 */
public abstract class CustomArgumentNode<T, N> extends AbstractCommandNode implements CustomArgumentType<@NotNull T, @NotNull N> {

    /**
     * 创建一个将 "this" (节点本身) 作为 ArgumentType 的 Builder。
     */
    @Override
    protected ArgumentBuilder<CommandSourceStack, ?> createBuilder() {
        return RequiredArgumentBuilder.argument(getName(), this);
    }

    /**
     * 智能地处理 Tab 补全建议。
     * 如果此节点实现了 ISuggestable 接口，则优先使用其提供的建议逻辑。
     * 否则，回退到 ArgumentType 的默认行为。
     */
    @Override
    public final <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        if (this instanceof ISuggestable) {
            try {
                // 类型转换是安全的，因为我们的框架只使用 CommandSourceStack
                @SuppressWarnings("unchecked") final CommandContext<CommandSourceStack> stackContext = (CommandContext<CommandSourceStack>) context;
                return ((ISuggestable) this).getSuggestionsProvider().getSuggestions(stackContext, builder);
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                return Suggestions.empty();
            }
        }
        // 否则，使用默认的建议行为
        return CustomArgumentType.super.listSuggestions(context, builder);
    }
}
