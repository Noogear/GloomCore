package cn.gloomcore.replacer;

import org.bukkit.OfflinePlayer;

import java.util.WeakHashMap;

public class ReplacerCache {
    private final OfflinePlayer player;
    private WeakHashMap<String, String> cache;

    public ReplacerCache(OfflinePlayer player) {
        cache = new WeakHashMap<>();
        this.player = player;
    }

    public void clear() {
        if (cache != null) {
            cache.clear();
        }
    }

    public void destroy() {
        cache = null;
    }

    public String get(String key) {
        return cache.computeIfAbsent(key, (k) -> ReplacerUtil.parsePapi(k, player));
    }

}
