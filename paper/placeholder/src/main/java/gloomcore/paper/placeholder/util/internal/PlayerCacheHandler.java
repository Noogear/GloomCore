package gloomcore.paper.placeholder.util.internal;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PlayerCacheHandler implements Listener {
    private final Map<UUID, Map<String, CacheEntry>> playerCache = new ConcurrentHashMap<>();

    public @Nullable String getOrUpdate(UUID uuid, String key, long intervalMillis, Supplier<String> supplier) {
        CacheEntry entry = playerCache
                .computeIfAbsent(uuid, k -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(key, k -> new CacheEntry(supplier.get(), System.currentTimeMillis()));

        long interval = System.currentTimeMillis() - entry.getLastUpdate();
        if (interval > intervalMillis || interval < 0) {
            return entry.update(supplier.get(), System.currentTimeMillis());
        }
        return entry.getText();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCache.remove(event.getPlayer().getUniqueId());
    }
}
