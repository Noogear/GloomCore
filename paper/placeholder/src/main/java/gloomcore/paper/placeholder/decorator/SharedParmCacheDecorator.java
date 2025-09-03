package gloomcore.paper.placeholder.decorator;

import gloomcore.paper.placeholder.internal.CacheEntry;
import gloomcore.paper.placeholder.internal.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SharedParmCacheDecorator implements Placeholder {
    private final long intervalMillis;
    private final Placeholder action;
    Map<Integer, CacheEntry> sharedCache = new ConcurrentHashMap<>();

    public SharedParmCacheDecorator(Placeholder action, long intervalMillis) {
        this.intervalMillis = intervalMillis;
        this.action = action;
    }

    @Override
    public @Nullable String apply(@Nullable Player player, @NotNull String[] args) {
        return sharedCache
                .computeIfAbsent(Arrays.hashCode(args), k -> new CacheEntry(action.apply(args), System.currentTimeMillis()))
                .getOrUpdate(intervalMillis, () -> action.apply(args));
    }
}
