package gloomcore.paper.placeholder.internal;

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
    private final Map<UUID, Object2ObjectOpenHashMap<String, CacheEntry>> playerCache = new ConcurrentHashMap<>();

    public @Nullable String getOrUpdate(UUID uuid, String key, long intervalMillis, Supplier<String> supplier) {
        return playerCache
                .computeIfAbsent(uuid, k -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(key, k -> new CacheEntry(supplier.get(), System.currentTimeMillis()))
                .getOrUpdate(intervalMillis, supplier);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerCache.remove(event.getPlayer().getUniqueId());
    }
}
