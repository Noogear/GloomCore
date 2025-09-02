package gloomcore.paper.placeholder.util.decorator;

import gloomcore.paper.placeholder.util.internal.CacheEntry;
import gloomcore.paper.placeholder.util.internal.FixedPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 一个只处理共享缓存的装饰器。
 */
public final class SharedFixedCacheDecorator implements FixedPlaceholder {
    private final FixedPlaceholder action;
    private final long intervalMillis;
    private final CacheEntry sharedCache;

    public SharedFixedCacheDecorator(FixedPlaceholder action, long intervalMillis) {
        this.action = action;
        this.intervalMillis = intervalMillis;
        this.sharedCache = new CacheEntry(action.apply(), System.currentTimeMillis());
    }

    @Override
    public @Nullable String apply(@Nullable Player player) {
        long interval = System.currentTimeMillis() - sharedCache.getLastUpdate();
        if (interval > intervalMillis || interval < 0) {
            return sharedCache.update(action.apply(), System.currentTimeMillis());
        }
        return sharedCache.getText();
    }
}
