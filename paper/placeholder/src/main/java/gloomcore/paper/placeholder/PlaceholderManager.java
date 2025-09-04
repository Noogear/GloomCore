package gloomcore.paper.placeholder;

import gloomcore.paper.placeholder.internal.CacheEntry;
import gloomcore.paper.placeholder.internal.FixedPlaceholder;
import gloomcore.paper.placeholder.internal.ParmPlaceholder;
import gloomcore.paper.placeholder.internal.PlaceholderNode;
import gloomcore.paper.placeholder.internal.key.PlaceholderKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.clip.placeholderapi.events.ExpansionsLoadedEvent;
import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlaceholderManager extends PlaceholderExpansion implements Cacheable, Listener {

    private final JavaPlugin plugin;
    private final String identifier;
    private final String author;
    private final String version;
    private final Object2ObjectOpenHashMap<String, FixedPlaceholder> fixedPlaceholderMap = new Object2ObjectOpenHashMap<>();
    private final PlaceholderNode rootNode;
    private final Map<UUID, Object2ObjectOpenHashMap<PlaceholderKey, CacheEntry>> playerCache = new ConcurrentHashMap<>();

    public PlaceholderManager(@NotNull JavaPlugin plugin, @NotNull String identifier, @NotNull String author, @NotNull String version) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.author = author;
        this.version = version;
        this.rootNode = new PlaceholderNode();
    }

    public @Nullable String getOrUpdate(UUID uuid, PlaceholderKey key, long intervalMillis, Supplier<String> supplier) {
        return playerCache
                .computeIfAbsent(uuid, k -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(key, k -> new CacheEntry(supplier.get(), System.currentTimeMillis()))
                .getOrUpdate(intervalMillis, supplier);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCache.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpansionsLoaded(ExpansionsLoadedEvent event) {
        if (!isRegistered()) {
            register();
        }
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

    public PlaceholderBuilder define(@NotNull String[] path) {
        return new PlaceholderBuilder(this, path);
    }

    void register(@NotNull String key, @NotNull FixedPlaceholder placeholder) {
        this.fixedPlaceholderMap.put(key, placeholder);
    }

    void register(@NotNull String[] path, @NotNull ParmPlaceholder placeholder) {
        this.rootNode.addPlaceholder(path, placeholder);
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (fixedPlaceholderMap.containsKey(params)) {
            return fixedPlaceholderMap.get(params).apply(player != null ? player.getPlayer() : null);
        }
        return rootNode.resolve(player != null ? player.getPlayer() : null, params);
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

    @Override
    public void clear() {
        playerCache.clear();
    }
}
