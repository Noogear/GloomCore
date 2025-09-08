package gloomcore.paper.command.framework;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import gloomcore.paper.command.interfaces.ISuggestable;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class CustomArgumentNode<T, N> extends AbstractCommandNode implements CustomArgumentType<@NotNull T, @NotNull N> {

    @Override
    protected final ArgumentBuilder<CommandSourceStack, ?> createBuilder() {
        return RequiredArgumentBuilder.argument(getName(), this);
    }

    /**
     * 将建议逻辑与框架的 ISuggestable 接口集成。
     * 如果此节点实现了 ISuggestable，则使用其提供的建议逻辑。
     * 否则，使用原生类型的建议逻辑。
     */
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        if (this instanceof ISuggestable) {
            @SuppressWarnings("unchecked") final CommandContext<CommandSourceStack> castedContext = (CommandContext<CommandSourceStack>) context;
            try {
                return ((ISuggestable) this).getSuggestionsProvider().getSuggestions(castedContext, builder);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return getNativeType().listSuggestions(context, builder);
    }
}
