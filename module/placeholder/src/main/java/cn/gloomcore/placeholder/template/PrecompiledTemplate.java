package cn.gloomcore.placeholder.template;

import cn.gloomcore.placeholder.placeholders.Placeholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrecompiledTemplate {
    private final Placeholder[] placeholders;

    private PrecompiledTemplate(@NotNull Placeholder[] placeholders) {
        this.placeholders = placeholders;
    }

    public String apply(@Nullable OfflinePlayer player) {
        StringBuilder builder = new StringBuilder();
        for (Placeholder placeholder : placeholders) {
            builder.append(placeholder.request(player));
        }
        return builder.toString();
    }

}
