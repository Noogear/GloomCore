package gloomcore.paper.placeholder.decorator;

import gloomcore.paper.placeholder.internal.CacheEntry;
import gloomcore.paper.placeholder.internal.FixedPlaceholder;
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
        return sharedCache.getOrUpdate(intervalMillis, action::apply);
    }
}
