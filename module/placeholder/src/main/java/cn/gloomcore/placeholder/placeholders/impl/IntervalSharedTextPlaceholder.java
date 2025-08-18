package cn.gloomcore.placeholder.placeholders.impl;

import cn.gloomcore.placeholder.CacheText;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class IntervalSharedTextPlaceholder extends SharedTextPlaceholder {
    private final AtomicReference<CacheText> cacheRef = new AtomicReference<>();
    private final long intervalMillis;

    public IntervalSharedTextPlaceholder(String original, long intervalMillis) {
        super(original);
        this.intervalMillis = intervalMillis;
        cacheRef.set(new CacheText(super.process(), System.currentTimeMillis()));
    }

    @Override
    public @NotNull String process(OfflinePlayer player) {
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
