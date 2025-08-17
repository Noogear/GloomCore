package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.AbstractTextPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerTextPlaceholder extends AbstractTextPlaceholder {
    public PlayerTextPlaceholder(String original) {
        super(original);
    }

    @Override
    public @NotNull String process(@Nullable OfflinePlayer player) {
        return parseText(player);
    }
}
