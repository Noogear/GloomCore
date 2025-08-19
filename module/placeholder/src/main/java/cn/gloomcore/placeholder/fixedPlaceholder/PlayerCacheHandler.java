package cn.gloomcore.placeholder.fixedPlaceholder;

import cn.gloomcore.placeholder.CacheText;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public class PlayerCacheHandler implements Listener {
    private final Object2ObjectOpenHashMap<UUID, Object2ObjectOpenHashMap<String, CacheText>> player2PlaceholdersCache;

    public PlayerCacheHandler() {
        this.player2PlaceholdersCache = new Object2ObjectOpenHashMap<>();
    }

    public @Nullable String getOrUpdate(UUID uuid, String params, long intervalMillis, Supplier<String> placeholderSupplier) {
        CacheText cacheText = this.player2PlaceholdersCache
                .computeIfAbsent(uuid, k -> new Object2ObjectOpenHashMap<>())
                .computeIfAbsent(params, k -> new CacheText(placeholderSupplier.get(), System.currentTimeMillis()));
        long interval = System.currentTimeMillis() - cacheText.getLastUpdate();
        if (interval > intervalMillis || interval < 0) {
            return cacheText.update(placeholderSupplier.get(), System.currentTimeMillis());
        } else return cacheText.getText();
    }

    public void remove(UUID uuid) {
        this.player2PlaceholdersCache.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        remove(event.getPlayer().getUniqueId());
    }


}
