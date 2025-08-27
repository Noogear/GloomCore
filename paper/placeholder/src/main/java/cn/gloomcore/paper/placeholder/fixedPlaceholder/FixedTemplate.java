package cn.gloomcore.paper.placeholder.fixedPlaceholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixedTemplate {
    private final FixedPlaceholder[] fixedPlaceholders;

    private FixedTemplate(@NotNull FixedPlaceholder[] fixedPlaceholders) {
        this.fixedPlaceholders = fixedPlaceholders;
    }

    public String apply(@Nullable Player player) {
        StringBuilder builder = new StringBuilder();
        for (FixedPlaceholder fixedPlaceholder : fixedPlaceholders) {
            builder.append(fixedPlaceholder.process(player));
        }
        return builder.toString();
    }

}
