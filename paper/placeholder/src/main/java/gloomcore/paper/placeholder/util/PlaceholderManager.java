package gloomcore.paper.placeholder.util;

import gloomcore.paper.placeholder.util.internal.Placeholder;
import gloomcore.paper.placeholder.util.internal.PlayerCacheHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaceholderManager extends PlaceholderExpansion {

    final PlayerCacheHandler playerCacheHandler;
    private final JavaPlugin plugin;
    private final String identifier;
    private final String author;
    private final String version;
    private final Map<String, Placeholder> fixedPlaceholderMap = new ConcurrentHashMap<>();

    public PlaceholderManager(@NotNull JavaPlugin plugin, @NotNull String identifier, @NotNull String author, @NotNull String version) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.playerCacheHandler = new PlayerCacheHandler();
        plugin.getServer().getPluginManager().registerEvents(playerCacheHandler, plugin);
    }

    /**
     * 开始定义一个新的占位符。
     * 这是注册占位符的推荐入口点。
     *
     * @param key 占位符的唯一标识 (例如 "player_health")。
     * @return 一个新的 PlaceholderBuilder 实例以进行链式配置。
     */
    public PlaceholderBuilder define(@NotNull String key) {
        return new PlaceholderBuilder(this, key);
    }

    void register(@NotNull String key, @NotNull Placeholder placeholder) {
        this.fixedPlaceholderMap.put(key, placeholder);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Placeholder placeholder = fixedPlaceholderMap.get(params);
        if (placeholder == null) {
            return null;
        }
        return player != null && player.isOnline() ? placeholder.apply(player.getPlayer()) : placeholder.apply();
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
    public @NotNull String getRequiredPlugin() {
        return plugin.getName();
    }
}
