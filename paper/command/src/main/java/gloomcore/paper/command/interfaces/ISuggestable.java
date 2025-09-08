package gloomcore.paper.command.interfaces;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;

/**
 * 实现此接口的命令节点可以提供自定义的 Tab 补全建议。
 */
@FunctionalInterface // 这是一个函数式接口，因为它只有一个抽象方法
public interface ISuggestable {

    /**
     * 获取此节点的 Tab 补全建议提供者。
     *
     * @return a SuggestionProvider instance.
     */
    SuggestionProvider<CommandSourceStack> getSuggestionsProvider();
}
