package gloomcore.paper.placeholder.util.decorator;

import gloomcore.paper.placeholder.util.internal.CacheEntry;
import gloomcore.paper.placeholder.util.internal.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedParmCacheDecorator implements Placeholder {
    private final long intervalMillis;
    private final Placeholder action;
    Map<String[], CacheEntry> sharedCache = new ConcurrentHashMap<>();

    public SharedParmCacheDecorator(Placeholder action, long intervalMillis) {
        this.intervalMillis = intervalMillis;
        this.action = action;
    }

    @Override
    public @Nullable String apply(@Nullable Player player, @NotNull String[] args) {
        CacheEntry entry = sharedCache.computeIfAbsent(args, k -> new CacheEntry(action.apply(k), System.currentTimeMillis()));
        long interval = System.currentTimeMillis() - entry.getLastUpdate();
        if (interval > intervalMillis || interval < 0) {
            return entry.update(action.apply(args), System.currentTimeMillis());
        }
        return entry.getText();
    }
}
