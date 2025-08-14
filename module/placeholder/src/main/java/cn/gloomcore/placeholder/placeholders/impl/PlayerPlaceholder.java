package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.AbstractPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPlaceholder extends AbstractPlaceholder {
    public PlayerPlaceholder(String original) {
        super(original);
    }

    @Override
    public @NotNull String request(@Nullable OfflinePlayer player) {
        return parseText(player);
    }
}
