package gloomcore.paper.placeholder.decorator;

import gloomcore.paper.placeholder.PlaceholderManager;
import gloomcore.paper.placeholder.internal.Placeholder;
import gloomcore.paper.placeholder.internal.key.PlaceholderKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 一个只处理玩家独立缓存的装饰器。
 */
public final class PlayerCacheDecorator implements Placeholder {
    private final Placeholder action;
    private final long intervalMillis;
    private final String key;
    private final PlaceholderManager playerCacheHandler;

    public PlayerCacheDecorator(Placeholder action, long intervalMillis, String key, PlaceholderManager playerCacheHandler) {
        this.action = action;
        this.intervalMillis = intervalMillis;
        this.key = key;
        this.playerCacheHandler = playerCacheHandler;
    }

    @Override
    public @Nullable String apply(@Nullable Player player, String[] args) {
        if (player == null) {
            return null;
        }
        return playerCacheHandler.getOrUpdate(player.getUniqueId(),
                PlaceholderKey.intern(key, args),
                intervalMillis, () -> action.apply(player, args));
    }
}
