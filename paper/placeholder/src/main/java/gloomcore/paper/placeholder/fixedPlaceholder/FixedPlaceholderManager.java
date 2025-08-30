package gloomcore.paper.placeholder.fixedPlaceholder;

import gloomcore.paper.placeholder.fixedPlaceholder.decorator.ActionDecorator;
import gloomcore.paper.placeholder.fixedPlaceholder.decorator.CacheDecorator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FixedPlaceholderManager extends PlaceholderExpansion {
    private final JavaPlugin plugin;
    private final String identifier;
    private final String author;
    private final String version;
    private final Object2ObjectOpenHashMap<String, FixedPlaceholder> string2Placeholder = new Object2ObjectOpenHashMap<>();
    private final PlayerCacheHandler playerCacheHandler;

    public FixedPlaceholderManager(@NotNull JavaPlugin plugin, @NotNull String identifier, @NotNull String author, @NotNull String version) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.playerCacheHandler = new PlayerCacheHandler();
        plugin.getServer().getPluginManager().registerEvents(playerCacheHandler, plugin);
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
    public @Nullable String getRequiredPlugin() {
        return plugin.getName();
    }

    @Override
    public @Nullable String onRequest(final OfflinePlayer player, @NotNull final String params) {
        if (player != null && player.isOnline()) {
            return onPlayerRequest(player.getPlayer(), params);
        }
        return onSharedRequest(params);
    }

    public FixedPlaceholderManager register(@NotNull String params, boolean isShared, long intervalMillis, ActionDecorator actionDecorator) {
        if (intervalMillis > 0) {
            return register(params, CacheDecorator.of(params, isShared, playerCacheHandler, intervalMillis, actionDecorator));
        }
        return register(params, actionDecorator);
    }

    public FixedPlaceholderManager register(@NotNull String params, @NotNull FixedPlaceholder fixedPlaceholder) {
        string2Placeholder.put(params, fixedPlaceholder);
        return this;
    }

    private @Nullable String onPlayerRequest(final Player player, @NotNull String params) {
        FixedPlaceholder fixedPlaceholder = string2Placeholder.get(params);
        return fixedPlaceholder != null ? fixedPlaceholder.process(player) : null;
    }

    private @Nullable String onSharedRequest(@NotNull String params) {
        FixedPlaceholder fixedPlaceholder = string2Placeholder.get(params);
        return fixedPlaceholder != null ? fixedPlaceholder.process() : null;
    }

}
