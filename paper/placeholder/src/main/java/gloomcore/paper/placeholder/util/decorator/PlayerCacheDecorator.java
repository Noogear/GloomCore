package gloomcore.paper.placeholder.util.decorator;

import gloomcore.paper.placeholder.util.internal.FixedPlaceholder;
import gloomcore.paper.placeholder.util.internal.PlaceholderAction;
import gloomcore.paper.placeholder.util.internal.PlayerCacheHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 一个只处理玩家独立缓存的装饰器。
 */
public final class PlayerCacheDecorator implements FixedPlaceholder {

    private final PlaceholderAction action;
    private final long intervalMillis;
    private final String key;
    private final PlayerCacheHandler playerCacheHandler;

    public PlayerCacheDecorator(PlaceholderAction action, long intervalMillis, String key, PlayerCacheHandler playerCacheHandler) {
        this.action = action;
        this.intervalMillis = intervalMillis;
        this.key = key;
        this.playerCacheHandler = playerCacheHandler;
    }

    @Override
    public @Nullable String apply(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        return playerCacheHandler.getOrUpdate(player.getUniqueId(), key, intervalMillis, () -> action.apply(player));
    }
}
