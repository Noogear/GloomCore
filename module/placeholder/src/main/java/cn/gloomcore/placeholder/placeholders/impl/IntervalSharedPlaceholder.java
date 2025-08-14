package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.placeholders.CacheText;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class IntervalSharedPlaceholder extends SharedPlaceholder {
    private final AtomicReference<CacheText> cacheRef = new AtomicReference<>();
    private final long intervalMillis;

    public IntervalSharedPlaceholder(String original, long intervalMillis) {
        super(original);
        this.intervalMillis = intervalMillis;
        cacheRef.set(new CacheText(super.request(), System.currentTimeMillis()));
    }

    @Override
    public @NotNull String request(OfflinePlayer player) {
        CacheText currentCache = cacheRef.get();
        if (System.currentTimeMillis() - currentCache.timestamp() < intervalMillis) {
            return currentCache.text();
        }
        String newText = parseText(null);
        CacheText newCache = new CacheText(newText, System.currentTimeMillis());
        if (cacheRef.compareAndSet(currentCache, newCache)) {
            return newText;
        } else {
            return cacheRef.get().text();
        }
    }

}
