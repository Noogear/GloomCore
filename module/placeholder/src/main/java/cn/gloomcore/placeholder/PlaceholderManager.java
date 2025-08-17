package cn.gloomcore.placeholder;

import cn.gloomcore.placeholder.placeholders.TextPlaceholder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderManager extends PlaceholderExpansion implements Listener {
    private final String identifier;
    private final String author;
    private final String version;
    private final Object2ObjectOpenHashMap<String, TextPlaceholder> string2Placeholder = new Object2ObjectOpenHashMap<>();

    public PlaceholderManager(@NotNull String identifier, @NotNull String author, @NotNull String version) {
        this.identifier = identifier;
        this.author = author;
        this.version = version;
    }

    @Override
    public @NotNull String getIdentifier() {
        return identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer player, @NotNull final String params) {
        if (player != null && player.isOnline()) {
            return onPlayerRequest(player.getPlayer(), params);
        }
        return onSharedRequest(params);
    }


    public PlaceholderManager register(@NotNull String params, boolean isShared, long intervalMillis) {
        return this;
    }

    public PlaceholderManager register(@NotNull String params, @NotNull TextPlaceholder placeholder) {
        string2Placeholder.put(params, placeholder);
        return this;
    }

    private @Nullable String onPlayerRequest(final Player player, @NotNull String params) {
        TextPlaceholder placeholder = string2Placeholder.get(params);
        return placeholder != null ? placeholder.process(player) : null;
    }

    private @Nullable String onSharedRequest(@NotNull String params) {
        TextPlaceholder placeholder = string2Placeholder.get(params);
        return placeholder != null ? placeholder.process() : null;
    }

}
