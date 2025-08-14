package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.AbstractPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SharedPlaceholder extends AbstractPlaceholder {
    public SharedPlaceholder(String original) {
        super(original);
    }

    @Override
    public @NotNull String request(OfflinePlayer player) {
        return parseText(null);
    }
}
