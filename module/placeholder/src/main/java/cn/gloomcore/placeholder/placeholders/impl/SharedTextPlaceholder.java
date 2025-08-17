package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.AbstractTextPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SharedTextPlaceholder extends AbstractTextPlaceholder {
    public SharedTextPlaceholder(String original) {
        super(original);
    }

    @Override
    public @NotNull String process(OfflinePlayer player) {
        return parseText(null);
    }
}
