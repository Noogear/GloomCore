package cn.gloomcore.placeholder.template;

import cn.gloomcore.placeholder.placeholders.TextPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrecompiledTemplate {
    private final TextPlaceholder[] placeholders;

    private PrecompiledTemplate(@NotNull TextPlaceholder[] placeholders) {
        this.placeholders = placeholders;
    }

    public String apply(@Nullable OfflinePlayer player) {
        StringBuilder builder = new StringBuilder();
        for (TextPlaceholder placeholder : placeholders) {
            builder.append(placeholder.process(player));
        }
        return builder.toString();
    }

}
